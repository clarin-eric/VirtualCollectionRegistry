package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.oai.provider.impl.OAIProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.model.*;
import eu.clarin.cmdi.virtualcollectionregistry.query.ParsedQuery;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VirtualCollectionRegistryImpl extends TxManager implements VirtualCollectionRegistry, InitializingBean, DisposableBean {

    private final static String REQUIRED_DB_VERSION = "1.6.0";

   //@Autowired
   // private final DataStore datastore; //TODO: replace with Spring managed EM?

    @Autowired
    @Qualifier("creation")
    private VirtualCollectionValidator validator;

    @Autowired
    private AdminUsersService adminUsersService;

    @Autowired
    private VirtualCollectionRegistryMaintenanceImpl maintenance;

    @Autowired
    private VirtualCollectionRegistryReferenceCheckImpl referenceCheck; //Checks collections for invalid references

    @Autowired
    private VirtualCollectionRegistryReferenceValidator referenceValidator; //Checks references for validity and gathers additional info for the reference

    @Autowired
    private CreatorService creatorService;
    
    private static final Logger logger
            = LoggerFactory.getLogger(VirtualCollectionRegistryImpl.class);
    
    private final AtomicBoolean intialized = new AtomicBoolean(false);

    // private final VirtualCollectionDao vcDao = new VirtualCollectionDaoImpl();
    private final VirtualCollectionDao vcDao = new VirtualCollectionDaoImplNamedQuery();

    @Autowired
    public VirtualCollectionRegistryImpl(DataStore datastore) {
        super(datastore);
    }

    /**
     * Scheduled executor service for the maintenance check
     *
     * //@see #maintenance(long)
     */
    private final ScheduledExecutorService maintenanceExecutor
            = createSingleThreadScheduledExecutor("VirtualCollectionRegistry-Maintenance");

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
        logger.info("Checking encoding settings:");
        logger.info("\tDefault Charset=" + Charset.defaultCharset());
        logger.info("\tfile.encoding=" + System.getProperty("file.encoding"));
        logger.info("\tSpecial character: [\u65E5]");
        
        try {
            checkDbVersion();
            long t1 = System.nanoTime();
            VirtualCollectionList collections = getAllVirtualCollections();
            creatorService.initialize(collections.getItems());
            long t2 = System.nanoTime();
            double tDelta = (t2-t1)/1000000.0;
            logger.debug(String.format("Initialized CreatorService in %.2fms; loaded %d creators.", tDelta, creatorService.getSize()));
            //Initialise schedulers
            maintenanceExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    maintenance.perform(new Date().getTime());
                }
            }, 30, 30, TimeUnit.SECONDS);
            maintenanceExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    logger.info("Running reference check");
                    referenceCheck.perform(new Date().getTime());
                }
            }, 1, 1, TimeUnit.DAYS);
            maintenanceExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    logger.trace("Running reference validation");
                    referenceValidator.perform(new Date().getTime());
                }
            }, 1, 1, TimeUnit.SECONDS);

            this.intialized.set(true);
            logger.info("virtual collection registry successfully intialized");
        } catch (RuntimeException e) {
            logger.error("error initalizing virtual collection registry", e);
            throw e;
        }
    }

    @Override
    public void destroy() throws VirtualCollectionRegistryException, InterruptedException {
        logger.info("Stopping Virtual Collection Registry maintenance schedule");
        maintenanceExecutor.shutdown();
        if (!maintenanceExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.warn("Timeout while waiting for maintenance thread to terminate, will try to shut down");
        }

        final OAIProvider oaiProvider = OAIProvider.instance();
        if (oaiProvider != null) {
            logger.info("Shutting down OAI provider");
            oaiProvider.shutdown();
        }
    }

    /**
     * Will store the specified collection; it will also set the owner according
     * to the specified principal and set its state to
     * {@link VirtualCollection.State#PRIVATE}
     *
     * @param principal owner principal
     * @param vc collection to store
     * @return identifier of the persisted collection
     * @throws VirtualCollectionRegistryException
     */
    @Override
    public long createVirtualCollection(Principal principal, VirtualCollection vc) throws VirtualCollectionRegistryException {
        long result = -1;
        try {
            beginTransaction();
            User user = getOrCreateUser(principal);
            result = createVirtualCollection(user, vc);
        } catch(Exception ex) {
            rollbackActiveTransaction("Failed to fetch or create user for principal="+ principal.getName()+".", ex);
        } finally {
            commitActiveTransaction();
        }
        return result;
    }

    public long createVirtualCollection(User user, VirtualCollection vc) throws VirtualCollectionRegistryException {
        if (user == null) {
            throw new NullPointerException("user == null");
        }

        logger.debug("creating virtual collection");

        final EntityManager em = datastore.getEntityManager();
        long result = -1;
        try {            
            beginTransaction();
            createCollection(em, vc, user);
            logger.debug("virtual collection created (id={})", vc.getId());
            result = vc.getId();
        } catch (Exception e) {
            rollbackActiveTransaction("error while creating virtual collection", e);
        } finally {
            commitActiveTransaction();
        }
        return result;
    }

    private long createCollection(EntityManager em, VirtualCollection vc, User user) throws VirtualCollectionRegistryException {
        if(vc.getId() != null) {
            vc.setId(null);
        }

        logger.debug("persisting new virtual collection");

        vc.setOwner(user);
        vc.setState(VirtualCollection.State.PRIVATE); // force new collection to be private

        // Make sure all relations are set from collection objects in managed state
        if(vc.getForkedFrom() != null && !em.contains(vc.getForkedFrom())) {
            logger.trace("Forked from is not managed, loading from database");
            VirtualCollection forkedFromVc = retrieveVirtualCollection(vc.getForkedFrom().getId());
            vc.setForkedFrom(forkedFromVc);
        }
        if(vc.getParent() != null && !em.contains(vc.getParent())) {
            logger.trace("Parent is not managed, loading from database");
            VirtualCollection parentVc = em.find(VirtualCollection.class,vc.getParent().getId());
            vc.setParent(parentVc);
        }
        if(vc.getChild() != null && !em.contains(vc.getChild())) {
            logger.trace("Child is not managed, loading from database");
            VirtualCollection childVc = em.find(VirtualCollection.class,vc.getChild().getId());
            vc.setChild(childVc);
        }
        if(vc.getRoot() != null && !em.contains(vc.getRoot())) {
            logger.trace("Root is not managed, loading from database");
            VirtualCollection rootVc = em.find(VirtualCollection.class,vc.getRoot().getId());
            vc.setRoot(rootVc);
        }

        validateCollection(vc);

        em.persist(vc);

        if(vc.getRoot() == null) {
            logger.trace("Updating collection with root reference to itself");
            vc.setRoot(vc);
            updateCollection(em, vc.getId(), vc);
        }

        return vc.getId();
    }

    @Override
    public User getOrCreateUser(String username) throws VirtualCollectionRegistryException {
        final EntityManager em = datastore.getEntityManager();
        User user = fetchUser(username);
        if (user == null) {
            user = new User(username);
            em.persist(user);
        }
        return user;
    }

    @Override
    public User getOrCreateUser(Principal principal) throws VirtualCollectionRegistryException {
        return getOrCreateUser(principal.getName());
    }

    private void validateCollection(VirtualCollection vc) throws NullPointerException, VirtualCollectionValidationException {
        if (vc == null) {
            throw new NullPointerException("vc new version == null");
        }

        logger.trace("validating virtual collection");

        try {
            validator.validate(vc);
        } catch(VirtualCollectionValidationException ex) {
            logger.info("Validation failed: ");
            for(String s: ex.getAllErrorsAsList()) {
                logger.info("   validation error: "+s);
            }
            throw ex;
        }
    }

    public long newVirtualCollectionVersion(Principal principal, long parentId, VirtualCollection newVersion) throws VirtualCollectionRegistryException {
        if (principal == null) {
            throw new NullPointerException("user == null");
        }

        long result = -1;
        EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            User user = getOrCreateUser(principal);
            VirtualCollection newVersionClone = newVersion.clone();
            long newVcId = createCollection(em, newVersionClone, user);
            updateCollectionWithChild(em, principal, parentId, newVcId);
            result = newVcId;
        } catch(Exception ex) {
            rollbackActiveTransaction("Failed to create new version", ex);
        } finally {
            commitActiveTransaction();
        }
        return result;
    }

    private long updateCollection(EntityManager em, long id, VirtualCollection vc) throws VirtualCollectionRegistryException {
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

        // update virtual collection
        logger.debug("Updating Virtual collection:\n{}", vc.toString());
        c.updateFrom(vc);
        validator.validate(c);
        em.merge(c);

        return c.getId();
    }

    private long updateCollectionWithChild(EntityManager em, Principal principal, long parentId, long childVcId) throws VirtualCollectionRegistryException {
        logger.debug("Adding child (id={}) to parent (id={})", childVcId, parentId);

        VirtualCollection parent = em.find(VirtualCollection.class,
                Long.valueOf(parentId), LockModeType.PESSIMISTIC_WRITE);

        if (parent == null) {
            logger.debug("parent virtual collection (id={}) not found", parentId);
            throw new VirtualCollectionNotFoundException(parentId);
        }
        if (!isAllowedToModify(principal, parent )) {
            throw new VirtualCollectionRegistryPermissionException(
                    "permission denied for user=\"" + principal.getName() + "\" to modify parent collection with id="+parentId);
        }

        VirtualCollection child = em.find(VirtualCollection.class,
                Long.valueOf(childVcId), LockModeType.PESSIMISTIC_WRITE);

        if (child == null) {
            logger.debug("child virtual collection (id={}) not found", childVcId);
            throw new VirtualCollectionNotFoundException(childVcId);
        }

        // update virtual collection with parent --> child relation
        parent.setChild(child);
        em.merge(parent);

        return parent.getId();
    }

    @Override
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
        
        validator.validate(vc);
        long result = -1;
        EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();

            if (!isAllowedToModify(principal, vc)) {
                throw new VirtualCollectionRegistryPermissionException(
                        "permission denied for user=\""+principal.getName()+"\" to modify collection with id="+id);
            }

            result = updateCollection(em, id, vc);
        } catch (VirtualCollectionRegistryException e) {
            em.getTransaction().rollback();
            rollbackActiveTransaction("failed to update virtual collecion (id="+id+"): "+e.getMessage(),e);
        } catch (Exception e) {
            rollbackActiveTransaction("error while updating virtual collection", e);
        } finally {
            commitActiveTransaction();
        }
        return result;
    }

    @Override
    public long deleteVirtualCollection(Principal principal, long id)
            throws VirtualCollectionRegistryException {
        if (principal == null) {
            throw new NullPointerException("principal == null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("id <= 0");
        }

        logger.debug("deleting virtual collection (id={})", id);
        long result = -1;
        EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            VirtualCollection vc = em.find(VirtualCollection.class,
                    Long.valueOf(id), LockModeType.PESSIMISTIC_WRITE);
            if ((vc == null) || vc.isDeleted()) {
                logger.debug("virtual collection (id={}) not found", id);
                throw new VirtualCollectionNotFoundException(id);
            }
            if (!isAllowedToModify(principal, vc)) {
                logger.debug("virtual collection (id={}) not owned by "
                        + "user '{}'", id, principal.getName());
                throw new VirtualCollectionRegistryPermissionException(
                        "permission denied for user \""
                        + principal.getName() + "\"");
            }
            //Non private collections or collections in error state cannot be 
            //deleted by non-admin users
            if (!vc.isPrivate() && vc.getState() != VirtualCollection.State.ERROR) {
                logger.debug("virtual collection (id={}) cannot be "
                        + "deleted (invalid state)", id);
                throw new VirtualCollectionRegistryPermissionException(
                        "virtual collection cannot be deleted");
            }
            vc.setState(VirtualCollection.State.DELETED);
            result = vc.getId();
        } catch (VirtualCollectionRegistryException e) {
            rollbackActiveTransaction("failed deleting virtual collecion (id="+id+"): "+e.getMessage(), e);
        } catch (Exception e) {
            rollbackActiveTransaction("error while deleting virtual collection", e);
        } finally {
            commitActiveTransaction();
        }
        return result;
    }

    @Override
    public VirtualCollection.State getVirtualCollectionState(long id)
            throws VirtualCollectionRegistryException {
        if (id <= 0) {
            throw new IllegalArgumentException("id <= 0");
        }

        logger.debug("retrieve virtual collection state (id={})", id);
        VirtualCollection.State result = null;
        EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            VirtualCollection vc
                    = em.find(VirtualCollection.class, Long.valueOf(id));
            if ((vc == null) || vc.isDeleted()) {
                logger.debug("virtual collection (id={}) not found", id);
                throw new VirtualCollectionNotFoundException(id);
            }
            result = vc.getState();
        } catch (VirtualCollectionRegistryException e) {
            rollbackActiveTransaction("", e);
        } catch (Exception e) {
            rollbackActiveTransaction("error while retrieving state of virtual collection", e);
        } finally {
            commitActiveTransaction();
        }
        return result;
    }

    
    @Override
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
        if ((state != VirtualCollection.State.PUBLIC_PENDING)
                && (state != VirtualCollection.State.PUBLIC_FROZEN_PENDING)
                && (state != VirtualCollection.State.PRIVATE)) {
            throw new IllegalArgumentException(
                    "only PUBLIC_PENDING, PUBLIC_FROZEN_PENDING or PRIVATE are allowed");
        }

        logger.debug("setting state virtual collection state (id={}) to '{}'",
                id, state);

        EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            VirtualCollection vc = em.find(VirtualCollection.class,
                    Long.valueOf(id), LockModeType.PESSIMISTIC_WRITE);
            if ((vc == null) || vc.isDeleted()) {
                logger.debug("virtual collection (id={}) not found", id);
                throw new VirtualCollectionNotFoundException(id);
            }
            if (!isAllowedToModify(principal, vc)) {
                logger.debug("virtual collection (id={}) not owned by "
                        + "user '{}'", id, principal.getName());
                throw new VirtualCollectionRegistryPermissionException(
                        "permission denied for user \""
                        + principal.getName() + "\"");
            }

            /*
             * XXX: deny update from public to private?
             */
            boolean update = false;
            switch (state) {
                case PRIVATE:
                    update = vc.getState() != state;
                    break;
                case PUBLIC_PENDING:
                    update = vc.getState() != VirtualCollection.State.PUBLIC;
                    break;
                case PUBLIC_FROZEN_PENDING:
                    update = vc.getState() != VirtualCollection.State.PUBLIC_FROZEN_PENDING;
                    break;
                default:
                    /* silence warning; update will stay false */
                    break;
            }
            if (update) {
                vc.setState(state);
                em.persist(vc);
            }
        } catch (VirtualCollectionRegistryException e) {
            rollbackActiveTransaction("", e);
        } catch (Exception e) {
            rollbackActiveTransaction("error while setting state of virtual collection", e);
        } finally {
            commitActiveTransaction();
        }
    }

    /**
     *
     * @param id identifier of the virtual collection to retrieve
     * @return the identified virtual collection, never null
     * @throws VirtualCollectionRegistryException if no virtual collection with
     * the specified identifier exists
     */
    @Override
    public VirtualCollection retrieveVirtualCollection(long id)
            throws VirtualCollectionRegistryException {
        if (id <= 0) {
            throw new IllegalArgumentException("id <= 0");
        }

        logger.trace("retrieve virtual collection (id={})", id);

        VirtualCollection vc = null;
        EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            vc = em.find(VirtualCollection.class, Long.valueOf(id));
            if ((vc == null) || vc.isDeleted()) {
                logger.debug("virtual collection (id={}) not found", id);
                throw new VirtualCollectionNotFoundException(id);
            }
            return vc;
        } catch (VirtualCollectionRegistryException e) {
            rollbackActiveTransaction("", e);
        } catch (Exception e) {
            rollbackActiveTransaction("error while retrieving virtual collection", e);
        } finally {
            commitActiveTransaction();
        }
        return vc;
    }

    public String getDbVersion() throws VirtualCollectionRegistryException {
        String dbVersion = null;
        final EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            TypedQuery<DbConfig> q = em.createNamedQuery("DbConfig.findByKey", DbConfig.class);
            q.setParameter("keyName", "db_version");
            DbConfig result = q.getSingleResult();
            dbVersion = result.getValue();
        } catch(NoResultException e) {
            rollbackActiveTransaction("No db_version key found in config table", e);
        } catch (Exception e) {
            rollbackActiveTransaction("error while verifying database version", e);
        } finally {
            commitActiveTransaction();
        }
        return dbVersion;
    }

    private void checkDbVersion() throws VirtualCollectionRegistryException {
        logger.info("checkDbVersion()");
        String dbVersion = getDbVersion();
        if(dbVersion == null) {
            throw new VirtualCollectionRegistryException(
                    "No db_version found. Expected "+REQUIRED_DB_VERSION);
        } else if(!dbVersion.equalsIgnoreCase(REQUIRED_DB_VERSION)) {
            throw new VirtualCollectionRegistryException(
                    "Incorrect db_version, expected "+REQUIRED_DB_VERSION+", got "+dbVersion);
        }
    }

    @Override
    public VirtualCollectionList getVirtualCollections(String query, int offset, int count) throws VirtualCollectionRegistryException {
        logger.info("getVirtualCollections()");

        List<VirtualCollection> results = null;
        long totalCount = 0;
        EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();

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
            
            for(VirtualCollection vc : results) {
                logger.info("Authors for "+vc.getName());
                for(String a : vc.getAuthors()) {
                    logger.info("\tAuthor: "+a);
                }
            }
        } catch (Exception e) {
            rollbackActiveTransaction("error while enumerating virtual collections", e);
        } finally {
            commitActiveTransaction();
        }
        return new VirtualCollectionList(results, offset, (int) totalCount);
    }

    @Override
    public VirtualCollectionList getVirtualCollections(Principal principal,
            String query, int offset, int count)
            throws VirtualCollectionRegistryException {
        logger.info("getVirtualCollections() with principal = "+principal.getName());
        if (principal == null) {
            throw new NullPointerException("principal == null");
        }
        List<VirtualCollection> results = new ArrayList<>();
        long totalCount = 0;
        final EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            /*
             * fetch user. if user is not found, he has not yet registered any
             * virtual collections, so just return an empty list
             */
            User user = fetchUser(principal.getName());
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
                    q = parsedQuery.getQuery(em, user, null);
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
/*
            if(results != null) {
                for (VirtualCollection vc : results) {
                    logger.info("Authors for " + vc.getName());
                    for (String a : vc.getAuthors()) {
                        logger.info("\tAuthor: " + a);
                    }
                }
            }
*/

        } catch (Exception e) {
            rollbackActiveTransaction("error while enumerating virtual collections", e);
        } finally {
            commitActiveTransaction();
        }
        return new VirtualCollectionList(results, offset, (int) totalCount);
    }
    
    public VirtualCollectionList getAllVirtualCollections() throws VirtualCollectionRegistryException {
        logger.trace("getAllVirtualCollections()");
        List<VirtualCollection> results = new ArrayList<>();
        final EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<VirtualCollection> cq = cb.createQuery(VirtualCollection.class);
            Root<VirtualCollection> rootEntry = cq.from(VirtualCollection.class);
            CriteriaQuery<VirtualCollection> all = cq.select(rootEntry);
            TypedQuery<VirtualCollection> allQuery = em.createQuery(all);
            results = allQuery.getResultList();
        } catch (Exception e) {
            rollbackActiveTransaction("error while enumerating virtual collections", e);
        } finally {
            commitActiveTransaction();
        }
        return new VirtualCollectionList(results, 0, results.size());
    }

    @Override
    public int getVirtualCollectionCount(QueryFactory qryFactory) throws VirtualCollectionRegistryException {
        logger.trace("getVirtualCollectionCount()");
        int result = -1;
        try {
            beginTransaction();
            qryFactory.addParam("vc_owner", "%%");
            result = vcDao.getVirtualCollectionCount(datastore.getEntityManager(), qryFactory);
        } catch(Exception ex) {
            rollbackActiveTransaction("error while fetching virtual collections", ex);
        } finally {
            commitActiveTransaction();
        }
        return result;
    }

    @Override
    public List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory qryFactory) throws VirtualCollectionRegistryException {
        logger.trace("getVirtualCollections()");
        List<VirtualCollection> result = new LinkedList<>();
        try {
            beginTransaction();
            qryFactory.addParam("vc_owner", "%%");
            result = vcDao.getVirtualCollections(datastore.getEntityManager(), first, count, qryFactory);
        } catch(Exception ex) {
            rollbackActiveTransaction("error while fetching virtual collections", ex);
        } finally {
            commitActiveTransaction();
        }
        return result;
    }

    @Override
    public List<String> getOrigins() throws VirtualCollectionRegistryException {
        List<String> origins = new ArrayList<>();
        EntityManager em = datastore.getEntityManager();
        try {
            beginTransaction();
            TypedQuery<String> q = em.createNamedQuery("VirtualCollection.findAllPublicOrigins", String.class);
            origins = q.getResultList();
        } catch (Exception e) {
            rollbackActiveTransaction("error while enumerating virtual collections to get all origins", e);
        } finally {
            commitActiveTransaction();
        }
        return origins;
    }

    @Override
    public List<User> getUsers() {
        final EntityManager em = datastore.getEntityManager();
        try {
            final CriteriaBuilder cb = em.getCriteriaBuilder();
            final CriteriaQuery<User> cq = cb.createQuery(User.class);
            final Root<User> root = cq.from(User.class);

            // select all users, sort by display name then name
            cq.select(root);
            cq.orderBy(
                    cb.asc(root.get(User_.displayName)),
                    cb.asc(root.get(User_.name)));

            beginTransaction();
            final TypedQuery<User> query = em.createQuery(cq);
            return query.getResultList();
        } finally {
            commitActiveTransaction();
        }
    }
/*
    public User createUser(Principal principal) throws VirtualCollectionRegistryException {
        return null;
    }

    public User createUserIfNotExists(Principal principal) throws VirtualCollectionRegistryException {
        return null;
    }
*/
    public User fetchUser(Principal principal) throws VirtualCollectionRegistryException {
        if(principal == null) {
            throw new VirtualCollectionRegistryException("Principal is required");
        }

        User user = null;
        try {
            beginTransaction();
            user = fetchUser(principal.getName());
        } catch (Exception e) {
            rollbackActiveTransaction("error while querying user with name="+principal.toString(), e);
        } finally {
            commitActiveTransaction();
        }
        return user;
    }

    private User fetchUser(String username) {
        final EntityManager em = datastore.getEntityManager();
        User user = null;
        try {
            beginTransaction();
            TypedQuery<User> q = em.createNamedQuery("User.findByName", User.class);
            q.setParameter("name", username);
            user = q.getSingleResult();
        } catch (NoResultException ex) {
            /* IGNORE */
        } finally {
            commitActiveTransaction();
        }
        return user;
    }

    /**
     * Creates a single thread scheduled executor with the specified thread name
     *
     * @param threadName name for new executor threads
     * @return
     */
    private static ScheduledExecutorService createSingleThreadScheduledExecutor(final String threadName) {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            // decorate default thread factory so that we can provide a
            // custom thread name
            final AtomicInteger i = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                final Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setName(threadName + "-" + i.addAndGet(1));
                return thread;
            }
        });
    }

    private boolean isAllowedToModify(Principal principal, VirtualCollection c) {
        // admin and owner are allowed to modify collections
        return adminUsersService.isAdmin(principal.getName())
                || c.getOwner().equalsPrincipal(principal);
    }
    
    public CreatorService getCreatorService() {
        return this.creatorService;
    }

    @Override
    public VirtualCollectionRegistryReferenceValidator getReferenceValidator() {
        return referenceValidator;
    }

} // class VirtualCollectionRegistry
