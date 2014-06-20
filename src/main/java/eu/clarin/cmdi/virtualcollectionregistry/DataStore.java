package eu.clarin.cmdi.virtualcollectionregistry;

import com.sun.jersey.api.spring.Autowire;
import java.util.Collections;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DataStore implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);
    private final EntityManagerFactory emf;
    private final ThreadLocal<EntityManager> em;

    @Autowired
    public DataStore(ServletContext servletContext) throws VirtualCollectionRegistryException {
        this(ServletUtils.createParameterMap(servletContext));
    }

    public DataStore(Map<String, String> config)
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

    @Override
    public void destroy() throws VirtualCollectionRegistryException {
        if (emf != null) {
            logger.info("Closing entity manager");
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
