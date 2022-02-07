package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

public class VirtualCollectionDaoImpl implements VirtualCollectionDao {
    private Logger logger = LoggerFactory.getLogger(VirtualCollectionDaoImpl.class);

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
     * @param em
     * @param factory
     * @return
     */
    private TypedQuery<VirtualCollection> getCollectionsQuery(EntityManager em, QueryFactory factory) {
        if(!em.getTransaction().isActive()) {
            throw new RuntimeException("Active transaction is requied");
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

        /*
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<VirtualCollection> cq = cb.createQuery(VirtualCollection.class);
        Root<VirtualCollection> root = cq.from(VirtualCollection.class);

        //Build subquery
        Subquery sub = cq.subquery(Long.class);
        Root subRoot = sub.from(VirtualCollection.class);
        sub.select(cb.max(subRoot.get("id"))); //Select max (aka latest) id. TODO: requires better approach to filter on latest version
        if(factory.getQueryOptions() != null) { //Apply any filter parameters to the subquery
            final Predicate where = factory.getQueryOptions().getWhere(cb, sub, subRoot);
            if (where != null) {
                sub.where(where);
            }
        }
        sub.groupBy(subRoot.get("root")); //Group by root column

        //Query collections where id's exist in the subquery result
        cq.where(cb.in(root.get("id")).value(sub));

        //Order main query result
        if(factory.getQueryOptions() != null) {
            final Order[] order = factory.getQueryOptions().getOrderBy(cb, root);
            if (order != null) {
                cq.orderBy(order);
            }
        }
*/
        return em.createQuery(cq.select(root));
    }

    public List<VirtualCollection> getVirtualCollections(EntityManager em, QueryFactory factory) {
        logger.trace("getVirtualCollections()");
        TypedQuery<VirtualCollection> query = getCollectionsQuery(em, factory);
        return query.getResultList();
    }

    @Override
    public List<VirtualCollection> getVirtualCollections(EntityManager em, int first, int count, QueryFactory factory) {
        logger.trace("getVirtualCollections(first={}, count={})", first, count);
        TypedQuery<VirtualCollection> query = getCollectionsQuery(em, factory);
        if (first > -1) {
            query.setFirstResult(first);
        }
        if (count >= 0) {
            query.setMaxResults(count);
        }
        return query.getResultList();
    }

    @Override
    public int getVirtualCollectionCount(EntityManager em, QueryFactory factory) throws VirtualCollectionRegistryException {
        logger.trace("getVirtualCollectionCount()");
        if(!em.getTransaction().isActive()) {
            throw new RuntimeException("Active transaction is requied");
        }

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
    }
}
