package eu.clarin.cmdi.virtualcollectionregistry;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
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

public class VirtualCollectionRegistry {
    private static final Logger logger =
        LoggerFactory.getLogger(VirtualCollectionRegistry.class);
    private static final VirtualCollectionRegistry s_instance =
        new VirtualCollectionRegistry();
    private AtomicBoolean intialized = new AtomicBoolean(false);
    private DataStore datastore = null;
    private PersistentIdentifierProvider pid_provider = null;
    private VirtualCollectionRegistryMarshaller marshaller = null;
    private Timer timer =
        new Timer("VirtualCollectionRegistry-Maintenance", true);

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
            this.datastore = new DataStore(config);
            this.pid_provider = PersistentIdentifierProvider
                    .createProvider(config);
            this.marshaller = new VirtualCollectionRegistryMarshaller();
            // setup OAIProvider
            OAIProvider.instance().setRepository(
                    new VirtualColletionRegistryOAIRepository(this));
            // setup VCR maintenance task
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    maintenance(this.scheduledExecutionTime());
                }
            }, 60000, 60000);
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
        timer.cancel();
        if (datastore != null) {
            datastore.destroy();
        }
        OAIProvider.instance().shutdown();
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
            em.persist(vc);
            em.getTransaction().commit();
            return vc.getId();
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
            VirtualCollection c = em.find(VirtualCollection.class, new Long(id),
                    LockModeType.PESSIMISTIC_WRITE);
            /*
             * Do not check for deleted state here, as we might want to
             * resurrect deleted virtual collections.
             */
            if (c == null) {
                logger.debug("virtual collection (id={}) not found", id);
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
            logger.debug("failed updating virtual collecion (id={}): {}", id,
                    e.getMessage());
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
            VirtualCollection vc = em.find(VirtualCollection.class,
                    new Long(id), LockModeType.PESSIMISTIC_WRITE);
            if ((vc == null) || vc.isDeleted()) {
                logger.debug("virtual collection (id={}) not found", id);
                throw new VirtualCollectionNotFoundException(id);
            }
            if (!vc.getOwner().equalsPrincipal(principal)) {
                logger.debug("virtual collection (id={}) not owned by " +
                        "user '{}'", id, principal.getName());
                throw new VirtualCollectionRegistryPermissionException(
                        "permission denied for user \"" +
                        principal.getName() + "\"");
            }
            if (!vc.isPrivate()) {
                logger.debug("virtual collection (id={}) cannot be " +
                        "deleted (invalid state)", id);
                throw new VirtualCollectionRegistryPermissionException(
                        "virtual collection cannot be deleted");
            }
            vc.setState(VirtualCollection.State.DELETED);
            em.getTransaction().commit();
            return vc.getId();
        } catch (VirtualCollectionRegistryException e) {
            logger.debug("failed deleting virtual collecion (id={}): {}", id,
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("error while deleting virtual collection", e);
            throw new VirtualCollectionRegistryException(
                    "error while deleting virtual collection", e);
        }
    }

    public VirtualCollection.State getVirtualCollectionState(long id)
            throws VirtualCollectionRegistryException {
        if (id <= 0) {
            throw new IllegalArgumentException("id <= 0");
        }

        logger.debug("retrieve virtual collection state (id={})", id);

        try {
            EntityManager em = datastore.getEntityManager();
            em.getTransaction().begin();
            VirtualCollection vc =
                em.find(VirtualCollection.class, new Long(id));
            em.getTransaction().commit();
            if ((vc == null) || vc.isDeleted()) {
                logger.debug("virtual collection (id={}) not found", id);
                throw new VirtualCollectionNotFoundException(id);
            }
            return vc.getState();
        } catch (VirtualCollectionRegistryException e) {
            throw e;
        } catch (Exception e) {
            logger.error(
                    "error while retrieving state of virtual collection", e);
            throw new VirtualCollectionRegistryException(
                    "error while retrieving state of virtual collection", e);
        }
    }

    public void setVirtualCollectionState(Principal principal, long id,
            VirtualCollection.State state)
            throws VirtualCollectionRegistryException {
        if (principal == null) {
            throw new NullPointerException("principal == null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("id <= 0");
        }
        if (state == null) {
            throw new NullPointerException("state == null");
        }
        if ((state != VirtualCollection.State.PUBLIC_PENDING) &&
            (state != VirtualCollection.State.PRIVATE)) {
            throw new IllegalArgumentException(
                    "only PUBLIC_PENDING or PRIVATE are allowed");
        }

        logger.debug("setting state virtual collection state (id={}) to '{}'",
                id, state);

        try {
            EntityManager em = datastore.getEntityManager();
            em.getTransaction().begin();
            VirtualCollection vc = em.find(VirtualCollection.class,
                    new Long(id), LockModeType.PESSIMISTIC_WRITE);
            if ((vc == null) || vc.isDeleted()) {
                logger.debug("virtual collection (id={}) not found", id);
                throw new VirtualCollectionNotFoundException(id);
            }
            if (!vc.getOwner().equalsPrincipal(principal)) {
                logger.debug("virtual collection (id={}) not owned by " +
                        "user '{}'", id, principal.getName());
                throw new VirtualCollectionRegistryPermissionException(
                        "permission denied for user \"" +
                        principal.getName() + "\"");
            }

            /*
             * XXX: deny update from public to private? 
             */
            boolean update = false;
            switch (state) {
            case PRIVATE:
                update =  vc.getState() != state;
                break;
            case PUBLIC_PENDING:
                update =  vc.getState() != VirtualCollection.State.PUBLIC;
                break;
            }
            if (update) {
                vc.setState(state);
                em.persist(vc);
            }
            em.getTransaction().commit();
        } catch (VirtualCollectionRegistryException e) {
            throw e;
        } catch (Exception e) {
            logger.error(
                    "error while setting state of virtual collection", e);
            throw new VirtualCollectionRegistryException(
                    "error while setting state of virtual collection", e);
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
            VirtualCollection vc =
                em.find(VirtualCollection.class, new Long(id));
            em.getTransaction().commit();
            if ((vc == null) || vc.isDeleted()) {
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

    public VirtualCollectionList getVirtualCollections(String query,
            int offset, int count) throws VirtualCollectionRegistryException {
        EntityManager em = datastore.getEntityManager();
        try {
            em.getTransaction().begin();

            // setup queries
            TypedQuery<Long> cq = null;
            TypedQuery<VirtualCollection> q = null;
            if (query != null) {
                ParsedQuery parsedQuery = ParsedQuery.parseQuery(query);
                if (logger.isDebugEnabled()) {
                    logger.debug(parsedQuery.getPrettyPrinted());
                }
                cq = parsedQuery.getCountQuery(em, null, VirtualCollection.State.PUBLIC);
                q = parsedQuery.getQuery(em, null, VirtualCollection.State.PUBLIC);
            } else {
                cq = em.createNamedQuery("VirtualCollection.countAllPublic",
                        Long.class);
                q = em.createNamedQuery("VirtualCollection.findAllPublic",
                        VirtualCollection.class);
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
            long totalCount = 0;

            em.getTransaction().begin();

            /*
             * fetch user. if user is not found, he has not yet registered any
             * virtual collections, so just return an empty list
             */
            User user = fetchUser(em, principal);
            if (user != null) {
                // setup queries
                TypedQuery<Long> cq = null;
                TypedQuery<VirtualCollection> q = null;
                if (query != null) {
                    ParsedQuery parsedQuery = ParsedQuery.parseQuery(query);
                    if (logger.isDebugEnabled()) {
                        logger.debug(parsedQuery.getPrettyPrinted());
                    }
                    cq = parsedQuery.getCountQuery(em, user, null);
                    q  = parsedQuery.getQuery(em, user, null);
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
//        } catch (QueryException e) {
//            throw new VirtualCollectionRegistryUsageException("query invalid",
//                    e);
        } catch (Exception e) {
            logger.error("error while enumerating virtual collections", e);
            throw new VirtualCollectionRegistryException(
                    "error while enumerating virtual collections", e);
        } finally {
            em.getTransaction().commit();
        }
    }

    private void maintenance(long now) {
        final Date nowDateAlloc = new Date(now - 30*1000);
        final Date nowDatePurge = new Date(now - 30*1000);
        
        EntityManager em = datastore.getEntityManager();
        try {
            /*
             * delayed allocation of persistent identifier
             */
            em.getTransaction().begin();
            TypedQuery<VirtualCollection> q =
                em.createNamedQuery("VirtualCollection.findAllByState",
                                    VirtualCollection.class);
            q.setParameter("state", VirtualCollection.State.PUBLIC_PENDING);
            q.setParameter("date", nowDateAlloc);
            q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            for (VirtualCollection vc : q.getResultList()) {
                if (vc.getPersistentIdentifier() == null) {
                    PersistentIdentifier pid = pid_provider.createIdentifier(vc);
                    vc.setPersistentIdentifier(pid);
                }
                vc.setState(VirtualCollection.State.PUBLIC);
                em.persist(vc);
                logger.debug("assigned pid (identifer='{}') to virtual" +
                        "collection (id={})",
                        vc.getPersistentIdentifier().getIdentifier(),
                        vc.getId());
            }
            em.getTransaction().commit();
            
            /*
             * delayed purging of deleted virtual collections 
             */
            em.getTransaction().begin();
            q.setParameter("state", VirtualCollection.State.DELETED);
            q.setParameter("date", nowDatePurge);
            q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            for (VirtualCollection vc : q.getResultList()) {
                vc.setState(VirtualCollection.State.DEAD);
                em.remove(vc);
                logger.debug("purged virtual collection (id={})", vc.getId());
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("error while doing maintenance", e);
        } finally {
            datastore.closeEntityManager();
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
