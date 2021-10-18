package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.TypedQuery;
import java.util.List;

public class VirtualCollectionDaoImplNamedQuery implements VirtualCollectionDao {
    private Logger logger = LoggerFactory.getLogger(VirtualCollectionDaoImpl.class);

    private TypedQuery<VirtualCollection> getCollectionsQuery(EntityManager em, QueryFactory factory) {
        if (!em.getTransaction().isActive()) {
            throw new RuntimeException("Active transaction is required");
        }

        TypedQuery<VirtualCollection> q = em.createNamedQuery("VirtualCollection.find", VirtualCollection.class);
        factory.applyParamsToQuery(q);
        return q;
    }

    @Override
    public List<VirtualCollection> getVirtualCollections(EntityManager em, QueryFactory factory) {
        logger.trace("getVirtualCollections()");
        return getCollectionsQuery(em, factory).getResultList();
    }

    @Override
    public List<VirtualCollection> getVirtualCollections(EntityManager em, int first, int count, QueryFactory factory) {
        logger.trace("getVirtualCollections(first={}, count={})", first, count);
        TypedQuery<VirtualCollection> q = getCollectionsQuery(em, factory);
        if (first > -1) {
            q.setFirstResult(first);
        }
        if (count > 0) {
            q.setMaxResults(count);
        }
        return q.getResultList();
    }

    @Override
    public int getVirtualCollectionCount(EntityManager em, QueryFactory factory) throws VirtualCollectionRegistryException {
        logger.trace("getVirtualCollectionCount()");
        if (!em.getTransaction().isActive()) {
            throw new RuntimeException("Active transaction is required");
        }
        TypedQuery<Long> q = em.createNamedQuery("VirtualCollection.findCount", Long.class);
        factory.applyParamsToQuery(q);
        long count = q.getSingleResult();
        return (int)count;
    }
}
