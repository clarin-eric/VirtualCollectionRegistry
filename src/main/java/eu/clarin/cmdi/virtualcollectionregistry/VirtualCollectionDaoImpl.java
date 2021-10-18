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
        return  em.createQuery(cq.select(root));
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
        if (count > 0) {
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
