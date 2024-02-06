package eu.clarin.cmdi.virtualcollectionregistry.db;

import eu.clarin.cmdi.virtualcollectionregistry.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionDao;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionDaoImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;

public class AbstractVirtualCollectionDaoTest {
    private final static Logger logger = LoggerFactory.getLogger(AbstractVirtualCollectionDaoTest.class);

    protected static DataStore datastore;

    protected static Connection connection;

    protected static VirtualCollectionDao dao;

    protected static final TestDatasetProvider datasetProvider = new TestDatasetProvider();

    public static VirtualCollectionDao getNewDaoWithNewDatastoreInstance() throws Exception {
        return new VirtualCollectionDaoImpl(
            new DataStore(
                Persistence.createEntityManagerFactory(
                    "VirtualCollectionStoreTest"))
        );
    }

    @BeforeClass
    public static void setUp() throws Exception {
        logger.info("Starting in-memory HSQL database for unit tests");
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");

        logger.info("Building JPA EntityManager for unit tests");
        datastore = new DataStore(Persistence.createEntityManagerFactory("VirtualCollectionStoreTest"));

        dao = new VirtualCollectionDaoImpl(datastore);
        datasetProvider.createDataset(datastore);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        logger.info("Stopping in-memory HSQL database.");
        logger.info("Shuting down Hibernate JPA layer.");
        if (datastore != null) {
            datastore.closeEntityManager();
            datastore.destroy();
        }
        connection.createStatement().execute("SHUTDOWN");
    }
}
