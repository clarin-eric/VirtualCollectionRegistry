package eu.clarin.cmdi.virtualcollectionregistry;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import eu.clarin.cmdi.virtualcollectionregistry.model.Handle;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.ResourceMetadata;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionValidator;

public class VirtualCollectionRegistry {
	private static final Logger logger = Logger
			.getLogger(VirtualCollectionRegistry.class.getName());
	private static VirtualCollectionRegistry s_instance =
		new VirtualCollectionRegistry();
	private AtomicBoolean intialized = new AtomicBoolean(false);
	private VirtualCollectionRegistryMarshaller marshaller;
	
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
			throw new IllegalArgumentException("config may not be null");
		}
		for (String key : config.keySet()) {
			logger.fine("XXX: " + key + " = \"" + config.get(key) + "\"");
		}
		marshaller = new VirtualCollectionRegistryMarshaller();
		intialized.set(true);
	}

	public void destroy() throws VirtualCollectionRegistryException {
	}

	public static VirtualCollectionRegistry instance() {
		if (!s_instance.intialized.get()) {
			throw new InternalError("virtual collection registry failed " +
					"to initialize correctly");
		}
		return s_instance;
	}

	public VirtualCollectionRegistryMarshaller getMarshaller() {
		return marshaller;
	}

	public long createVirtualCollection(Principal principal,
			VirtualCollection vc) throws VirtualCollectionRegistryException {
		if (principal == null) {
			throw new IllegalArgumentException("principal == null");
		}
		if (vc == null) {
			throw new IllegalArgumentException("vc == null");
		}

		VirtualCollectionValidator validator =
			new VirtualCollectionValidator();
		validator.validate(vc);

		try {
			EntityManager em = DataStore.instance().getEntityManager();
			User user = null;

			
			em.getTransaction().begin();
			// FIXME: use TypedQuery variant when migrating to JPA 2.0
			Query q = em.createNamedQuery("User.findByName");
			q.setParameter("name", principal.getName());
			try {
				user = (User) q.getSingleResult();
			} catch (NoResultException e) {
				user = new User(principal.getName());
				em.persist(user);
			}
			vc.setOwner(user);
			vc.setPid(Handle.createPid());
			for (Resource resource : vc.getResources()) {
				if (resource instanceof ResourceMetadata) {
					ResourceMetadata md = (ResourceMetadata) resource;
					md.setPid(Handle.createPid());
				}
			}
			em.persist(vc);			
			em.getTransaction().commit();

			// XXX: for test PID service
			em.getTransaction().begin();
			em.persist(new Handle(vc.getPid(), Handle.Type.COLLECTION, vc.getId()));
			for (Resource resource : vc.getResources()) {
				if (resource instanceof ResourceMetadata) {
					ResourceMetadata md = (ResourceMetadata) resource;
					em.persist(new Handle(md.getPid(), Handle.Type.METADATA,
											md.getId()));
				}
			}
			em.getTransaction().commit();
			return vc.getId();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "create", e);
			throw new VirtualCollectionRegistryException("create", e);
		}
	}

	public long updateVirtualCollection(Principal principal, long id, VirtualCollection vc)
			throws VirtualCollectionRegistryException {
		if (principal == null) {
			throw new IllegalArgumentException("principal == null");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("id <= 0");
		}
		if (vc == null) {
			throw new IllegalArgumentException("vc == null");
		}
		
		VirtualCollectionValidator validator =
			new VirtualCollectionValidator();
		validator.validate(vc);

		try {
			EntityManager em = DataStore.instance().getEntityManager();
			em.getTransaction().begin();
			VirtualCollection c = em.find(VirtualCollection.class, new Long(id));
			if (c == null) {
				throw new VirtualCollectionNotFoundException(id);
			}
			if (!c.getOwner().equalsPrincipal(principal)) {
				throw new VirtualCollectionRegistryPermissionException(
						"permission denied for user " + principal.getName());
			}
			c.updateFrom(vc);
			HashSet<String> newPids = new HashSet<String>();
			for (Resource resource : c.getResources()) {
				if (resource instanceof ResourceMetadata) {
					ResourceMetadata md = (ResourceMetadata) resource;
					if (md.getPid() == null) {
						String pid = Handle.createPid();
						md.setPid(pid);
						newPids.add(pid);
					}
				}
			}
			em.getTransaction().commit();
			em.getTransaction().begin();
			for (Resource resource : c.getResources()) {
				if (resource instanceof ResourceMetadata) {
					ResourceMetadata md = (ResourceMetadata) resource;
					if (newPids.contains(md.getPid())) {
						em.persist(new Handle(md.getPid(),
								Handle.Type.METADATA, md.getId()));
					}
				}
			}
			em.getTransaction().commit();
			return vc.getId();
		} catch (VirtualCollectionRegistryException e) {
			throw e;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "update", e);
			throw new VirtualCollectionRegistryException("update", e);
		}
	}

	public long deleteVirtualCollection(Principal principal, long id)
			throws VirtualCollectionRegistryException {
		if (principal == null) {
			throw new IllegalArgumentException("principal == null");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("id <= 0");
		}

		try {
			EntityManager em = DataStore.instance().getEntityManager();
			em.getTransaction().begin();
			VirtualCollection vc = em.find(VirtualCollection.class, new Long(id));
			if (vc == null) {
				throw new VirtualCollectionNotFoundException(id);
			}
			if (!vc.getOwner().equalsPrincipal(principal)) {
				throw new VirtualCollectionRegistryPermissionException(
						"permission denied for user " + principal.getName());
			}
			em.remove(vc);
			em.getTransaction().commit();
			return vc.getId();
		} catch (VirtualCollectionRegistryException e) {
			throw e;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "delete", e);
			throw new VirtualCollectionRegistryException("delete", e);
		}
	}

	public VirtualCollection retrieveVirtualCollection(long id)
			throws VirtualCollectionRegistryException {
		if (id <= 0) {
			throw new IllegalArgumentException("id <= 0");
		}

		try {
			EntityManager em = DataStore.instance().getEntityManager();
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
			logger.log(Level.SEVERE, "get", e);
			throw new VirtualCollectionRegistryException("get", e);
		}
	}

	public ResourceMetadata retrieveMetadataResource(long id)
			throws VirtualCollectionRegistryException {
		if (id <= 0) {
			throw new IllegalArgumentException("id <= 0");
		}

		try {
			EntityManager em = DataStore.instance().getEntityManager();
			em.getTransaction().begin();
			ResourceMetadata md = em.find(ResourceMetadata.class, new Long(id));
			em.getTransaction().commit();
			if (md == null) {
				throw new VirtualCollectionNotFoundException(id);
			}
			return md;
		} catch (VirtualCollectionRegistryException e) {
			throw e;
		} catch (Exception e) {
			throw new VirtualCollectionRegistryException("get metadata", e);
		}
			
	}

	@SuppressWarnings("unchecked")
	public List<VirtualCollection> getVirtualCollections()
			throws VirtualCollectionRegistryException {
		EntityManager em = DataStore.instance().getEntityManager();
		try {
			em.getTransaction().begin();
			// FIXME: use TypedQuery variant when migrating to JPA 2.0
			return (List<VirtualCollection>) em.createNamedQuery(
					"VirtualCollection.findAll").getResultList();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "list", e);
			throw new VirtualCollectionRegistryException("list", e);
		} finally {
			em.getTransaction().commit();
		}
	}

} // class VirtualCollectionRegistry
