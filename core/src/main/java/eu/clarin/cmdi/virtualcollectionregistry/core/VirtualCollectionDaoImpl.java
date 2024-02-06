package eu.clarin.cmdi.virtualcollectionregistry.core;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

public class VirtualCollectionDaoImpl extends TxManager implements VirtualCollectionDao {
    private Logger logger = LoggerFactory.getLogger(VirtualCollectionDaoImpl.class);

    private final DataStore datastore;

    public VirtualCollectionDaoImpl(DataStore datastore) {
        this.datastore = datastore;
    }

    private EntityManager getEntityManager() {
        if(datastore == null) {
            throw new NullPointerException("Datastore is required");
        }
        return datastore.getEntityManager();
    }

    /**
     *
     * id       root    parent  child   state       public_leaf
     * 0        NULL    NULL    NULL    *           true                //unversioned
     *
     * 1        1       NULL    2       public      false               //versioned
     * 2        1       1       NULL    public      true                //versioned
     *
     * 3        3       NULL    4       public      true                //versioned
     * 4        3       3       NULL    private     false               //versioned
     *
     * 5        5       NULL    6       public      false               //versioned
     * 6        5       5       7       public      true                //versioned         child is null does not work for public collections
     * 7        5       6       NULL    private     false               //versioned
     *
     * @param factory
     * @return
     */
    private TypedQuery<VirtualCollection> getCollectionsQuery(QueryFactory factory) {
        EntityManager em = getEntityManager();
        if(!em.getTransaction().isActive()) {
            throw new RuntimeException("Active transaction is required");
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<VirtualCollection> cq = cb.createQuery(VirtualCollection.class);
        Root<VirtualCollection> root = cq.from(VirtualCollection.class);
        if(factory.getQueryOptions() != null) {
            final Predicate where = factory.getQueryOptions().getWhere(cb, cq, root);
            if (where != null) {
                cq.where(where);
            }
            final Order[] order = factory.getQueryOptions().getOrderBy(cb, root);
            if (order != null) {
                cq.orderBy(order);
            }
        }

        return em.createQuery(cq.select(root));
    }

    private User fetchUser(String username) {
        EntityManager em = getEntityManager();
        User user = null;
        try {
            beginTransaction(em);
            TypedQuery<User> q = em.createNamedQuery("User.findByName", User.class);
            q.setParameter("name", username);
            user = q.getSingleResult();
        } catch (NoResultException ex) {
            /* IGNORE */
        } finally {
            commitActiveTransaction(em);
        }
        return user;
    }

    public User getOrCreateUser(String username) throws VirtualCollectionRegistryException {
        EntityManager em = getEntityManager();
        User user = fetchUser(username);
        if (user == null) {
            user = new User(username);
            em.persist(user);
        }
        return user;
    }

    private long createCollection(VirtualCollection vc, User owner) throws VirtualCollectionRegistryException {
        EntityManager em = getEntityManager();
        if(vc == null) {
            throw new IllegalArgumentException("Virtualcollection cannot be null");
        }
        if(owner == null) {
            throw new IllegalArgumentException("Virtualcollection owner cannot be null");
        }
        if(!em.contains(owner)) {
            throw new IllegalArgumentException("Virtualcollection owner must be a managed entity");
        }

        vc.setOwner(owner);
        vc.setState(VirtualCollection.State.PRIVATE); // force new collection to be private
        if(vc.getRoot() == null) {
            vc.setRoot(vc);
        }

        // Make sure all relations are set from collection objects in managed state
        if(vc.getForkedFrom() != null && vc.getForkedFrom().getId() != null && !em.contains(vc.getForkedFrom())) {
            logger.trace("Forked from is not managed, loading from database");
            VirtualCollection forkedFromVc = getVirtualCollection(vc.getForkedFrom().getId());
            vc.setForkedFrom(forkedFromVc);
        }
        if(vc.getParent() != null && vc.getParent().getId() != null && !em.contains(vc.getParent())) {
            logger.trace("Parent is not managed, loading from database");
            VirtualCollection parentVc = getVirtualCollection(vc.getParent().getId());
            vc.setParent(parentVc);
        }
        if(vc.getChild() != null && vc.getChild().getId() != null && !em.contains(vc.getChild())) {
            logger.trace("Child is not managed, loading from database");
            VirtualCollection childVc = getVirtualCollection(vc.getChild().getId());
            vc.setChild(childVc);
        }
        if(vc.getRoot() != null && vc.getRoot().getId() != null && !em.contains(vc.getRoot())) {
            logger.trace("Root is not managed, loading from database");
            VirtualCollection rootVc = getVirtualCollection(vc.getRoot().getId());
            vc.setRoot(rootVc);
        }

        em.persist(vc);

        return vc.getId();
    }

    @Override
    public void persist(VirtualCollection vc) throws VirtualCollectionRegistryException {
        EntityManager em = getEntityManager();
        try {
            beginTransaction(em);
            //Clear any special id values. E.g. forked collections
            if(vc.getId() != null && (vc.getId() == VirtualCollectionFactory.FORKED_ID) || vc.getId() == VirtualCollectionFactory.SUBMITTED_ID){
                vc.setId((null));
                vc.setRoot(vc);
            }

            if(vc.getId() == null) {
                logger.trace("Creating new collection");
                User owner = getOrCreateUser(vc.getOwner().getName());
                createCollection(vc, owner);
            } else {
                logger.trace("Updating existing collection, id = "+vc.getId());
                em.merge(vc); //how to handle unmanaged case?
            }
            logger.info("Persisted collection: " +
                    "id=" + vc.getId() + ", " +
                    "name=" + vc.getName() + ", " +
                    "state=" + vc.getState().toString() + ", " +
                    "public_leaf=" + vc.isPublicLeaf() + ", " +
                    "parent_id=" + (vc.getParent() != null ? vc.getParent().getId() : "no parent") + ", " +
                    "parent_p" +
                    "public_leaf=" + (vc.getParent() != null ? vc.getParent().isPublicLeaf() : "no parent"));
        } catch(Exception ex) {
            rollbackActiveTransaction(em, "", ex);
        } finally {
            commitActiveTransaction(em);

        }
    }

    /**
     *
     * @param id identifier of the virtual collection to retrieve
     * @return the identified virtual collection, never null
     * @throws VirtualCollectionRegistryException if no virtual collection with
     * the specified identifier exists
     */
    public VirtualCollection getVirtualCollection(long id) throws VirtualCollectionRegistryException {
        EntityManager em = getEntityManager();

        if (id <= 0) {
            throw new IllegalArgumentException("id <= 0");
        }

        logger.trace("retrieve virtual collection (id={})", id);
        
        VirtualCollection vc = null;
        try {
            beginTransaction(em);
            vc = em.find(VirtualCollection.class, Long.valueOf(id));
            if ((vc == null) || vc.isDeleted()) {
                logger.debug("virtual collection (id={}) not found", id);
                throw new VirtualCollectionNotFoundException(id);
            }
            return vc;
        } catch (VirtualCollectionRegistryException e) {
            rollbackActiveTransaction(em, "", e);
        } catch (Exception e) {
            rollbackActiveTransaction(em,"error while retrieving virtual collection", e);
        } finally {
            commitActiveTransaction(em);
        }
        return vc;
    }

    @Override
    public List<VirtualCollection> getVirtualCollections(QueryFactory factory) {
        EntityManager em = getEntityManager();
        logger.trace("getVirtualCollections()");

        try {
            beginTransaction(em);
            TypedQuery<VirtualCollection> query = getCollectionsQuery(factory);
            return query.getResultList();
        } finally {
            commitActiveTransaction(em);
        }
    }

    @Override
    public List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory factory) {
        EntityManager em = getEntityManager();
        logger.trace("getVirtualCollections(first={}, count={})", first, count);

        try {
            beginTransaction(em);

            TypedQuery<VirtualCollection> query = getCollectionsQuery(factory);
            if (first > -1) {
                query.setFirstResult(first);
            }
            if (count >= 0) {
                query.setMaxResults(count);
            }
            return query.getResultList();
        } finally {
            commitActiveTransaction(em);
        }
    }

    @Override
    public int getVirtualCollectionCount(QueryFactory factory) throws VirtualCollectionRegistryException {
        logger.trace("getVirtualCollectionCount()");
        EntityManager em = getEntityManager();

        try {
            beginTransaction(em);

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<VirtualCollection> root = cq.from(VirtualCollection.class);
            if (factory.getQueryOptions() != null) {
                Predicate where = factory.getQueryOptions().getWhere(cb, cq, root);
                if (where != null) {
                    cq.where(where);
                }
            }

            TypedQuery<Long> query = em.createQuery(cq.select(cb.count(root)));

            final long count = query.getSingleResult();
            if (count >= Integer.MAX_VALUE) {
                throw new VirtualCollectionRegistryException("resultset too large (count >= Integer.MAX_VALUE)");
            }

            return (int) count;
        } finally {
            commitActiveTransaction(em);
        }
    }
}
