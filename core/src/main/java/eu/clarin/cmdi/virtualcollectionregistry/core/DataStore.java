package eu.clarin.cmdi.virtualcollectionregistry.core;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import jakarta.servlet.ServletContext;
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

    public DataStore(Map<String, String> config) throws VirtualCollectionRegistryException {
        this(Persistence.createEntityManagerFactory("VirtualCollectionStore", config));
        /*
        try {   
            emf = Persistence.createEntityManagerFactory(
                    "VirtualCollectionStore", config);
            em = new ThreadLocal<EntityManager>() {
                @Override
                protected EntityManager initialValue() {
                    if (emf == null) {
                        throw new InternalError(
                                "JPA not initalizied correctly");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.trace("Creating new thread local entity manager in thread {}", Thread.currentThread().getName());
                    }
                    return emf.createEntityManager();
                }
            };
        } catch (Exception e) {
            logger.error("error initializing data store", e);
            throw new VirtualCollectionRegistryException(
                    "error initializing", e);
        }
        */
    }

    public DataStore(EntityManagerFactory emf) throws VirtualCollectionRegistryException {
        this.emf = emf;
        try {
            em = new ThreadLocal<EntityManager>() {
                @Override
                protected EntityManager initialValue() {
                    if (emf == null) {
                        throw new InternalError(
                                "JPA not initalizied correctly");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.trace("Creating new thread local entity manager in thread {}", Thread.currentThread().getName());
                    }
                    return emf.createEntityManager();
                }
            };
        } catch (Exception e) {
            logger.error("error initializing data store", e);
            throw new VirtualCollectionRegistryException(
                    "error initializing", e);
        }
        logger.trace("data store was successfully initialized");
    }

    @Override
    public void destroy() throws VirtualCollectionRegistryException {
        if (emf != null) {
            logger.info("Closing entity manager factory");
            emf.close();
        }
    }

    public EntityManager getEntityManager() {
        logger.trace("Entity manager requested in thread");
        final EntityManager manager = em.get();
        if (logger.isTraceEnabled()) {
            logger.trace("Returning entity manager {} (isOpen = {})", manager, manager.isOpen());
        }
        return manager;
    }

    public void closeEntityManager() {
        logger.trace("Closing of entity manager requested");
        EntityManager manager = em.get();
        if (manager != null) {
            em.remove();
            EntityTransaction tx = manager.getTransaction();
            if (tx.isActive()) {
                logger.debug("Entity manager has active transaction, rolling back");
                tx.rollback();
            }
            logger.trace("Closing entity manager");
            manager.close();
        }
    }

} // class DataStore
