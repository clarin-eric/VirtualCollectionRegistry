package eu.clarin.cmdi.virtualcollectionregistry;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import eu.clarin.cmdi.virtualcollectionregistry.model.Handle;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionValidator;
import eu.clarin.cmdi.virtualcollectionregistry.query.ParsedQuery;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryException;

public class VirtualCollectionRegistry {
	private static final Logger logger =
		Logger.getLogger(VirtualCollectionRegistry.class.getName());
	private static final VirtualCollectionRegistry s_instance =
		new VirtualCollectionRegistry();
	private AtomicBoolean intialized = new AtomicBoolean(false);
	private DataStore datastore = null;
	private VirtualCollectionRegistryMarshaller marshaller = null;
	
	private VirtualCollectionRegistry() {
		super();
	}

	public static void initalize(Map<String, String> config)
			throws VirtualCollectionRegistryException {
		s_instance.doInitalize(config);
	}

	private void doInitalize(Map<String, String> config)
			throws VirtualCollectionRegistryException {
		if (intialized.get()) {
			throw new VirtualCollectionRegistryException("already initialized");
		}
		logger.fine("initialize ...");
		if (config == null) {
			config = Collections.emptyMap();
		}
		for (String key : config.keySet()) {
			logger.fine("XXX: " + key + " = \"" + config.get(key) + "\"");
		}
		datastore  = new DataStore(config);
		marshaller = new VirtualCollectionRegistryMarshaller();
		intialized.set(true);
	}

	public void destroy() throws VirtualCollectionRegistryException {
		if (datastore != null) {
			datastore.destroy();
		}
	}

	public static VirtualCollectionRegistry instance() {
		if (!s_instance.intialized.get()) {
			throw new InternalError("virtual collection registry failed " +
					"to initialize correctly");
		}
		return s_instance;
	}

	public DataStore getDataStore() {
		return datastore;
	}

	public VirtualCollectionRegistryMarshaller getMarshaller() {
		return marshaller;
	}

	public long createVirtualCollection(Principal principal,
			VirtualCollection vc) throws VirtualCollectionRegistryException {
		if (principal == null) {
			throw new NullPointerException("principal == null");
		}
		if (vc == null) {
			throw new NullPointerException("vc == null");
		}

		VirtualCollectionValidator validator =
			new VirtualCollectionValidator();
		validator.validate(vc);

		try {
			EntityManager em = datastore.getEntityManager();
			em.getTransaction().begin();

			// fetch user, if user does not exist create new
			User user = fetchUser(em, principal);
			if (user == null) {
				user = new User(principal.getName());
				em.persist(user);
			}

			// store virtual collection
			vc.setOwner(user);
			vc.setPid(Handle.createPid());
			em.persist(vc);			
			em.getTransaction().commit();

			// XXX: for test PID service
			em.getTransaction().begin();
			em.persist(new Handle(vc.getPid(), Handle.Type.COLLECTION, vc.getId()));
			em.getTransaction().commit();
			return vc.getId();
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					   "error while creating virtual collection", e);
			throw new VirtualCollectionRegistryException(
					"error while creating virtual collection", e);
		}
	}

	public long updateVirtualCollection(Principal principal, long id,
			VirtualCollection vc) throws VirtualCollectionRegistryException {
		if (principal == null) {
			throw new NullPointerException("principal == null");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("id <= 0");
		}
		if (vc == null) {
			throw new NullPointerException("vc == null");
		}
		
		VirtualCollectionValidator validator =
			new VirtualCollectionValidator();
		validator.validate(vc);

		try {
			EntityManager em = datastore.getEntityManager();
			em.getTransaction().begin();
			VirtualCollection c = em.find(VirtualCollection.class, new Long(id));
			if (c == null) {
				throw new VirtualCollectionNotFoundException(id);
			}
			if (!c.getOwner().equalsPrincipal(principal)) {
				throw new VirtualCollectionRegistryPermissionException(
						"permission denied for user \"" +
						principal.getName() + "\"");
			}
			c.updateFrom(vc);
			validator.validate(c);
			em.getTransaction().commit();
			return vc.getId();
		} catch (VirtualCollectionRegistryException e) {
			throw e;
		} catch (Exception e) {
			logger.log(Level.SEVERE,
                       "error while updating virtual collection", e);
			throw new VirtualCollectionRegistryException(
					"error while updating virtual collection", e);
		}
	}

	public long deleteVirtualCollection(Principal principal, long id)
			throws VirtualCollectionRegistryException {
		if (principal == null) {
			throw new NullPointerException("principal == null");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("id <= 0");
		}

		try {
			EntityManager em = datastore.getEntityManager();
			em.getTransaction().begin();
			VirtualCollection vc = em.find(VirtualCollection.class, new Long(id));
			if (vc == null) {
				throw new VirtualCollectionNotFoundException(id);
			}
			if (!vc.getOwner().equalsPrincipal(principal)) {
				throw new VirtualCollectionRegistryPermissionException(
						"permission denied for user \"" +
						principal.getName() + "\"");
			}
			em.remove(vc);
			em.getTransaction().commit();
			return vc.getId();
		} catch (VirtualCollectionRegistryException e) {
			throw e;
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					   "error while deleting virtual collection", e);
			throw new VirtualCollectionRegistryException(
					"error while deleting virtual collection", e);
		}
	}

	public VirtualCollection retrieveVirtualCollection(long id)
			throws VirtualCollectionRegistryException {
		if (id <= 0) {
			throw new IllegalArgumentException("id <= 0");
		}

		try {
			EntityManager em = datastore.getEntityManager();
			em.getTransaction().begin();
			VirtualCollection vc = em.find(VirtualCollection.class,
					new Long(id));
			em.getTransaction().commit();
			if (vc == null) {
				throw new VirtualCollectionNotFoundException(id);
			}
			return vc;
		} catch (VirtualCollectionRegistryException e) {
			throw e;
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					   "error while retrieving virtual collection", e);
			throw new VirtualCollectionRegistryException(
					"error while retrieving virtual collection", e);
		}
	}

	public VirtualCollectionList getVirtualCollections(String query,
			int offset, int count) throws VirtualCollectionRegistryException {
		EntityManager em = datastore.getEntityManager();
		try {
			em.getTransaction().begin();

			// setup queries
			TypedQuery<Long>              cq = null;
			TypedQuery<VirtualCollection>  q = null;
			if (query != null) {
				ParsedQuery parsedQuery = ParsedQuery.parseQuery(em, query);
				cq = parsedQuery.getCountQuery(null);
				q = parsedQuery.getQuery(null);
			} else {
				cq = em.createNamedQuery("VirtualCollection.countAll",
						Long.class);
				q = em.createNamedQuery("VirtualCollection.findAll",
						VirtualCollection.class);
			}

			// commence query ...
			List<VirtualCollection> results = null;
			long totalCount = cq.getSingleResult();

			// optimization; don't query, if we won't get any results
			if ( totalCount > 0) {
				if (offset > 0) {
					q.setFirstResult(offset);
				}
				if (count > 0) {
					q.setMaxResults(count);
				}
				results = q.getResultList();
			}
			return new VirtualCollectionList(results, offset, (int) totalCount);
		} catch (QueryException e) {
			throw new VirtualCollectionRegistryUsageException(
			        "query invalid", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					   "error while enumerating virtual collections", e);
			throw new VirtualCollectionRegistryException(
					"error while enumerating virtual collections", e);
		} finally {
			em.getTransaction().commit();
		}
	}

	public VirtualCollectionList getVirtualCollections(Principal principal,
			String query, int offset, int count)
			throws VirtualCollectionRegistryException {
		if (principal == null) {
			throw new NullPointerException("principal == null");
		}
		EntityManager em = datastore.getEntityManager();
		try {
			em.getTransaction().begin();

			// fetch user
			User user = fetchUser(em, principal);
			if (user == null) {
				throw new VirtualCollectionRegistryPermissionException("user " +
						principal.getName() + " does not exist");
			}

			// setup queries
			TypedQuery<Long>              cq = null;
			TypedQuery<VirtualCollection>  q = null;
			if (query != null) {
				ParsedQuery parsedQuery = ParsedQuery.parseQuery(em, query);
				cq = parsedQuery.getCountQuery(user);
				q = parsedQuery.getQuery(user);
			} else {
				cq = em.createNamedQuery("VirtualCollection.countByOwner", Long.class);
				cq.setParameter("owner", user);
				q = em.createNamedQuery("VirtualCollection.findByOwner",
						VirtualCollection.class);
				q.setParameter("owner", user);
			}

			// commence query ...
			List<VirtualCollection> results = null;
			long totalCount = cq.getSingleResult();

			// optimization; don't query, if we won't get any results
			if (totalCount > 0) {
				if (offset > 0) {
					q.setFirstResult(offset);
				}
				if (count > 0) {
					q.setMaxResults(count);
				}
				results = q.getResultList();
			}
			return new VirtualCollectionList(results, offset, (int) totalCount);
		} catch (QueryException e) {
			throw new VirtualCollectionRegistryUsageException(
			        "query invalid", e);
		} catch (VirtualCollectionRegistryException e) {
			throw e;
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					   "error while enumerating virtual collections", e);
			throw new VirtualCollectionRegistryException(
					"error while enumerating virtual collections", e);
		} finally {
			em.getTransaction().commit();
		}
	}

	private static User fetchUser(EntityManager em, Principal principal) {
		User user = null;
		try {
			TypedQuery<User> q =
				em.createNamedQuery("User.findByName", User.class);
			q.setParameter("name", principal.getName());
			user = q.getSingleResult();
		} catch (NoResultException e) {
			/* IGNORE */
		}
		return user;
	}

} // class VirtualCollectionRegistry
