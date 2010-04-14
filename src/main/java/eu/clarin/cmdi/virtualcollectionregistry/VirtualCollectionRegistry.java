package eu.clarin.cmdi.virtualcollectionregistry;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.model.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.model.PersistentIdentifierProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionValidator;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIProvider;
import eu.clarin.cmdi.virtualcollectionregistry.query.ParsedQuery;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryException;

public class VirtualCollectionRegistry {
	private static final Logger logger =
		LoggerFactory.getLogger(VirtualCollectionRegistry.class);
	private static final VirtualCollectionRegistry s_instance =
		new VirtualCollectionRegistry();
	private AtomicBoolean intialized = new AtomicBoolean(false);
	private DataStore datastore = null;
	private PersistentIdentifierProvider pid_provider = null;
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
		logger.debug("initializing virtual collection registry ...");
		if (config != null) {
			config = Collections.unmodifiableMap(config);
		} else {
			config = Collections.emptyMap();
		}
		try {
			// XXX: the whole config / setup stuff is not very beautiful
			this.datastore    = new DataStore(config);
			this.pid_provider = PersistentIdentifierProvider.createProvider(config);
			this.marshaller   = new VirtualCollectionRegistryMarshaller();
			// setup OAIProvider
			OAIProvider.instance()
				.setRepository(new VirtualColletionRegistryOAIRepository(this));
			this.intialized.set(true);
			logger.info("virtual collection registry successfully intialized");
		} catch (RuntimeException e) {
			logger.error("error initalizing virtual collection registry", e);
			throw e;
		} catch (OAIException e) {
			logger.error("error initalizing virtual collection registry", e);
			throw new VirtualCollectionRegistryException(
					"setting OAI repository failed", e);
		} catch (VirtualCollectionRegistryException e) {
			logger.error("error initalizing virtual collection registry", e);
			throw e;
		}
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

		logger.debug("creating virtual collection");

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
			vc.createUUID();
			em.persist(vc);
			em.getTransaction().commit();

			PersistentIdentifier pid = pid_provider.createIdentifier(vc);
			em.getTransaction().begin();
			em.persist(pid);
			em.getTransaction().commit();
			logger.debug("created virtual collection (id={}, pid={})",
					     vc.getId(), pid.getIdentifier());
			return vc.getId();
		} catch (VirtualCollectionRegistryException e) {
			logger.debug("failed creating virtual collecion: {}",
					     e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("error while creating virtual collection", e);
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

		logger.debug("updating virtual collection (id={})", id);

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
			logger.debug("updated virtual collection (id={})", vc.getId());
			return vc.getId();
		} catch (VirtualCollectionRegistryException e) {
			logger.debug("failed updating virtual collecion (id={}): {}",
						 id, e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("error while updating virtual collection", e);
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

		logger.debug("deleting virtual collection (id={})", id);

		try {
			EntityManager em = datastore.getEntityManager();
			em.getTransaction().begin();
			VirtualCollection vc = em.find(VirtualCollection.class, new Long(id));
			if (vc == null) {
				logger.debug("virtual collection (id={}) not found", id);
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
			logger.debug("failed deleting virtual collecion (id={}): {}",
					 id, e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("error while deleting virtual collection", e);
			throw new VirtualCollectionRegistryException(
					"error while deleting virtual collection", e);
		}
	}

	public VirtualCollection retrieveVirtualCollection(long id)
			throws VirtualCollectionRegistryException {
		if (id <= 0) {
			throw new IllegalArgumentException("id <= 0");
		}

		logger.debug("retrieve virtual collection (id={})", id);

		try {
			EntityManager em = datastore.getEntityManager();
			em.getTransaction().begin();
			VirtualCollection vc = em.find(VirtualCollection.class,
					new Long(id));
			em.getTransaction().commit();
			if (vc == null) {
				logger.debug("virtual collection (id={}) not found", id);
				throw new VirtualCollectionNotFoundException(id);
			}
			return vc;
		} catch (VirtualCollectionRegistryException e) {
			throw e;
		} catch (Exception e) {
			logger.error("error while retrieving virtual collection", e);
			throw new VirtualCollectionRegistryException(
					"error while retrieving virtual collection", e);
		}
	}

	public VirtualCollection retrieveVirtualCollection(String uuid)
			throws VirtualCollectionRegistryException {
		if (uuid == null) {
			throw new NullPointerException("uuid == null");
		}
		uuid = uuid.trim();
		if (uuid.isEmpty()) {
			throw new IllegalArgumentException("uuid is empty");
		}

		logger.debug("retrieve virtual collection (uuid={})", uuid);

		try {
			EntityManager em = datastore.getEntityManager();
			em.getTransaction().begin();
			TypedQuery<VirtualCollection> q =
				em.createNamedQuery("VirtualCollection.byUUID",
									VirtualCollection.class);
			q.setParameter("uuid", uuid);
			VirtualCollection vc = q.getSingleResult();
			em.getTransaction().commit();
			return vc;
		} catch (NoResultException e) {
			logger.debug("virtual collection (uuid={}) not found", uuid);
			throw new VirtualCollectionNotFoundException(uuid);
		} catch (Exception e) {
			logger.error("error while retrieving virtual collection", e);
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
			logger.error("error while enumerating virtual collections", e);
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
			List<VirtualCollection> results = null;
			long totalCount                 = 0;

			em.getTransaction().begin();

			/*
			 *  fetch user. if user is not found, he has not yet registered
			 *  any virtual collections, so just return an empty list
			 */
			User user = fetchUser(em, principal);
			if (user != null) {
				// setup queries
				TypedQuery<Long>              cq = null;
				TypedQuery<VirtualCollection>  q = null;
				if (query != null) {
					ParsedQuery parsedQuery = ParsedQuery.parseQuery(em, query);
					cq = parsedQuery.getCountQuery(user);
					q = parsedQuery.getQuery(user);
				} else {
					cq = em.createNamedQuery("VirtualCollection.countByOwner",
											 Long.class);
					cq.setParameter("owner", user);
					q = em.createNamedQuery("VirtualCollection.findByOwner",
							VirtualCollection.class);
					q.setParameter("owner", user);
				}
	
				// commence query ...
				totalCount = cq.getSingleResult();
	
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
			}
			return new VirtualCollectionList(results, offset, (int) totalCount);
		} catch (QueryException e) {
			throw new VirtualCollectionRegistryUsageException(
			        "query invalid", e);
		} catch (Exception e) {
			logger.error("error while enumerating virtual collections", e);
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
