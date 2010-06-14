package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStore {
    private static final Logger logger =
        LoggerFactory.getLogger(DataStore.class);
    private final EntityManagerFactory emf;
    private final ThreadLocal<EntityManager> em;

    DataStore(Map<String, String> config)
        throws VirtualCollectionRegistryException {
        try {
            emf = Persistence.createEntityManagerFactory(
                    "VirtualCollectionStore", config);
            em = new ThreadLocal<EntityManager>() {
                protected EntityManager initialValue() {
                    if (emf == null) {
                        throw new InternalError(
                                "JPA not initalizied correctly");
                    }
                    return emf.createEntityManager();
                }
            };
        } catch (Exception e) {
            logger.error("error initializing data store", e);
            throw new VirtualCollectionRegistryException(
                    "error initializing", e);
        }
        logger.debug("data store was successfully initialized");
    }

    public void destroy() throws VirtualCollectionRegistryException {
        if (emf != null) {
            emf.close();
        }
    }

    public EntityManager getEntityManager() {
        return em.get();
    }

    public void closeEntityManager() {
        EntityManager manager = em.get();
        if (manager != null) {
            em.remove();
            EntityTransaction tx = manager.getTransaction();
            if (tx.isActive()) {
                tx.rollback();
            }
            manager.close();
        }
    }

} // class DataStore
