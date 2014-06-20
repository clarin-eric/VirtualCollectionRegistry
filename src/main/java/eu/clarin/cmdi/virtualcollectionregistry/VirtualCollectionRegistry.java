package eu.clarin.cmdi.virtualcollectionregistry;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.oai.provider.impl.OAIProvider;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifierProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.query.ParsedQuery;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VirtualCollectionRegistry implements InitializingBean, DisposableBean {

    @Autowired
    private DataStore datastore; //TODO: replace with Spring managed EM
    @Autowired
    private PersistentIdentifierProvider pid_provider;
    @Autowired
    private VirtualCollectionMarshaller marshaller;
    @Autowired
    private OAIProvider oaiProvider;
    
    private static final Logger logger =
        LoggerFactory.getLogger(VirtualCollectionRegistry.class);
    private final AtomicBoolean intialized = new AtomicBoolean(false);
    private final Timer timer =
        new Timer("VirtualCollectionRegistry-Maintenance", true);
    
    @Override
    public void afterPropertiesSet() throws VirtualCollectionRegistryException {
        // called by Spring directly after Bean construction
        doInitalize();
    }

    private void doInitalize() throws VirtualCollectionRegistryException {
        if (intialized.get()) {
            throw new VirtualCollectionRegistryException("already initialized");
        }
        logger.info("Initializing virtual collection registry ...");
        try {
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
        }
    }

    @Override
    public void destroy() throws VirtualCollectionRegistryException {
        logger.info("Stopping Virtual Collection Registry maintenance schedule");
        timer.cancel();
        
        logger.info("Shutting down OAI provider");
        oaiProvider.shutdown();
    }

    public DataStore getDataStore() {
        return datastore;
    }

    public VirtualCollectionMarshaller getMarshaller() {
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
            VirtualCollection c = em.find(VirtualCollection.class,
                    Long.valueOf(id), LockModeType.PESSIMISTIC_WRITE);
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
            
            // update virtual collection
            c.updateFrom(vc);

            validator.validate(c);
            em.merge(c);
            em.getTransaction().commit();
            logger.debug("updated virtual collection (id={})", vc.getId());
            return c.getId();
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
                    Long.valueOf(id), LockModeType.PESSIMISTIC_WRITE);
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
                em.find(VirtualCollection.class, Long.valueOf(id));
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
                    Long.valueOf(id), LockModeType.PESSIMISTIC_WRITE);
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
    
    /**
     * 
     * @param id identifier of the virtual collection to retrieve
     * @return the identified virtual collection, never null
     * @throws VirtualCollectionRegistryException if no virtual collection with
     * the specified identifier exists
     */
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
                em.find(VirtualCollection.class, Long.valueOf(id));
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
            /*
             *  FIXME: offset == -1 is temporary hack for just fetching
             *  total count; re-factor to have fetch-count and fetch-data
             *  methods!
             */
            if ((totalCount > 0) && (offset > -1)) {
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
            EntityTransaction tx = em.getTransaction();
            if ((tx != null) && !tx.getRollbackOnly()) {
                tx.commit();
            }
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
                /*
                 *  FIXME: offset == -1 is temporary hack for just fetching
                 *  total count; re-factor to have fetch-count and fetch-data
                 *  methods!
                 */
                if ((totalCount > 0) && (offset > -1)) {
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
        } catch (Exception e) {
            logger.error("error while enumerating virtual collections", e);
            throw new VirtualCollectionRegistryException(
                    "error while enumerating virtual collections", e);
        } finally {
            EntityTransaction tx = em.getTransaction();
            if ((tx != null) && !tx.getRollbackOnly()) {
                tx.commit();
            }
        }
    }

    public int getVirtualCollectionCount(QueryOptions options)
            throws VirtualCollectionRegistryException {
        EntityManager em = datastore.getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<VirtualCollection> root = cq.from(VirtualCollection.class);
            if (options != null) {
                Predicate where = options.getWhere(cb, cq, root);
                if (where != null) {
                    cq.where(where);
                }
            }
            em.getTransaction().begin();
            TypedQuery<Long> query =
                em.createQuery(cq.select(cb.count(root)));
            final long count = query.getSingleResult();
            if (count >= Integer.MAX_VALUE) {
                throw new VirtualCollectionRegistryException(
                        "resultset too large");
            }
            return (int) count;
        } catch (Exception e) {
            logger.error("error while counting virtual collections", e);
            throw new VirtualCollectionRegistryException(
                    "error while counting virtual collections", e);
        } finally {
            EntityTransaction tx = em.getTransaction();
            if ((tx != null) && tx.isActive() && !tx.getRollbackOnly()) {
                tx.commit();
            }
        }
    }
    
    public List<VirtualCollection> getVirtualCollections(
            int first, int count, QueryOptions options)
            throws VirtualCollectionRegistryException {
        EntityManager em = datastore.getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<VirtualCollection> cq =
                cb.createQuery(VirtualCollection.class);
            Root<VirtualCollection> root = cq.from(VirtualCollection.class);
            if (options != null) {
                final Predicate where = options.getWhere(cb, cq, root);
                if (where != null) {
                    cq.where(where);
                }
                final Order[] order = options.getOrderBy(cb, root);
                if (order != null) {
                    cq.orderBy(order);
                }
            }
            em.getTransaction().begin();
            TypedQuery<VirtualCollection> query =
                em.createQuery(cq.select(root));
            if (first > -1) {
                query.setFirstResult(first);
            }
            if (count > 0) {
                query.setMaxResults(count);
            }
            return query.getResultList();
        } catch (Exception e) {
            logger.error("error while fetching virtual collections", e);
            throw new VirtualCollectionRegistryException(
                    "error while fetching virtual collections", e);
        } finally {
            EntityTransaction tx = em.getTransaction();
            if ((tx != null) && tx.isActive() && !tx.getRollbackOnly()) {
                tx.commit();
            }
        }
    }

    private void maintenance(long now) {
        // allocate persistent identifier roughly after 30 seconds 
        final Date nowDateAlloc = new Date(now - 30*1000);
        // (for now) purge deleted collection roughly after 30 seconds
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
