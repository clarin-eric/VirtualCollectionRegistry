package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class DataStore {
	private class ThreadLocalEntityManager extends ThreadLocal<EntityManager> {
		protected EntityManager initialValue() {
			if (emf == null) {
				throw new InternalError("JPA not initalizied correctly");
			}
			return emf.createEntityManager();
		}
	} // inner class ThreadLocalEntityManager
	private static final Logger logger = Logger.getLogger(DataStore.class
			.getName());
	private static DataStore s_instance = new DataStore();
	private EntityManagerFactory emf = null;
	private ThreadLocalEntityManager em = new ThreadLocalEntityManager();

	private DataStore() {
	}

	public void initalize(Map<String, String> config)
			throws VirtualCollectionRegistryException {
		logger.fine("initializing");
		if (config != null) {
			config = new HashMap<String, String>();
		}
		try {
			emf = Persistence.createEntityManagerFactory(
					"VirtualCollectionStore", config);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error initializing data store", e);
			throw new VirtualCollectionRegistryException(
					"error initializing", e);
		}
		logger.finer("data store was successfully initialized");
	}

	public void destroy() throws VirtualCollectionRegistryException {
		if (emf != null) {
			emf.close();
		}
	}

	public static DataStore instance() {
		return s_instance;
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
}
