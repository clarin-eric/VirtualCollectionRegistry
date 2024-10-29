package eu.clarin.cmdi.virtualcollectionregistry.db;

import eu.clarin.cmdi.virtualcollectionregistry.core.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionDao;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionDaoImpl;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;

public class DbEnvironment {

    private final static Logger logger = LoggerFactory.getLogger(DbEnvironment.class);

    private final DataStore datastore;

    private final Connection connection;

    private final VirtualCollectionDao dao;

    public static DbEnvironment initialize() throws Exception {
        return new DbEnvironment();
    }

    private DbEnvironment() throws Exception {
        logger.info("Starting in-memory HSQL database for unit tests");
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");

        logger.info("Building JPA EntityManager for unit tests");
        datastore = new DataStore(Persistence.createEntityManagerFactory("VirtualCollectionStoreTest"));
        dao = new VirtualCollectionDaoImpl(this.datastore);
    }

    public EntityManager getEntityManager() {
        return datastore.getEntityManager();
    }

    public VirtualCollectionDao getVirtualCollectionDao() {
        return this.dao;
    }

    public DbEnvironment createDataset() throws VirtualCollectionRegistryException {
        new TestDatasetProvider().createDataset(datastore);
        return this;
    }

    public void destroy() throws Exception {
        logger.info("Stopping in-memory HSQL database.");
        logger.info("Shuting down Hibernate JPA layer.");

        if (datastore != null) {
            datastore.closeEntityManager();
            datastore.destroy();
        }

        connection.createStatement().execute("SHUTDOWN");
    }
}
