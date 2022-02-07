package eu.clarin.cmdi.virtualcollectionregistry.db;

import eu.clarin.cmdi.virtualcollectionregistry.*;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;
import java.util.List;

/**
 * Test all implementations of VirtualCollectionDao
 *
 * References:
 *  https://eskatos.wordpress.com/2007/10/15/unit-test-jpa-entities-with-in-memory-database/
 *  HSQLDB data types: http://hsqldb.org/doc/2.0/guide/sqlgeneral-chapt.html
 *  HSQLDB SQL issues: http://www.hsqldb.org/doc/1.8/guide/ch02.html
 *
 *  https://www.baeldung.com/junit-src-test-resources-directory-path
 */
public class VirtualCollectionDaoTest {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionDaoTest.class);

    private static EntityManagerFactory emFactory;

    private static EntityManager em;

    private static Connection connection;

    private final VirtualCollectionDao dao = new VirtualCollectionDaoImpl();

    private static final TestDatasetProvider datasetProvider = new TestDatasetProvider();

    @BeforeClass
    public static void setUp() throws Exception {
        logger.info("Starting in-memory HSQL database for unit tests");
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");

        logger.info("Building JPA EntityManager for unit tests");
        emFactory = Persistence.createEntityManagerFactory("VirtualCollectionStoreTest");
        em = emFactory.createEntityManager();

        datasetProvider.createDataset(em);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        logger.info("Shuting down Hibernate JPA layer.");
        if (em != null) {
            em.close();
        }
        if (emFactory != null) {
            emFactory.close();
        }
        logger.info("Stopping in-memory HSQL database.");
        connection.createStatement().execute("SHUTDOWN");
    }

    @Test
    public void getPublicVirtualCollectionsCountTest() throws VirtualCollectionRegistryException {
        em.getTransaction().begin();
        int count = dao.getVirtualCollectionCount(em, getPublicQueryFactory());
        em.getTransaction().commit();

        Assert.assertEquals(TestDatasetProvider.publicCollectionCount, count);
    }

    @Test
    public void getPrivateVirtualCollectionsCountTest() throws VirtualCollectionRegistryException {
        em.getTransaction().begin();
        int count = dao.getVirtualCollectionCount(em, getPrivateQueryFactory(TestDatasetProvider.owner1Name));
        em.getTransaction().commit();
        Assert.assertEquals(TestDatasetProvider.owner1PrivateCollectionCount, count);

        em.getTransaction().begin();
        count = dao.getVirtualCollectionCount(em, getPrivateQueryFactory(TestDatasetProvider.owner2Name));
        em.getTransaction().commit();
        Assert.assertEquals(TestDatasetProvider.owner2PrivateCollectionCount, count);
    }

    @Test
    public void getVirtualCollectionsWithOffset() throws VirtualCollectionRegistryException {
        em.getTransaction().begin();
        List<VirtualCollection> collections = dao.getVirtualCollections(em, 0, 0, getPublicQueryFactory());
        em.getTransaction().commit();
        Assert.assertEquals(0, collections.size());

        em.getTransaction().begin();
        collections = dao.getVirtualCollections(em, 0, 1, getPublicQueryFactory());
        em.getTransaction().commit();
        Assert.assertEquals(1, collections.size());

        em.getTransaction().begin();
        collections = dao.getVirtualCollections(em, 0, 1000, getPublicQueryFactory());
        em.getTransaction().commit();
        Assert.assertEquals(TestDatasetProvider.publicCollectionCount, collections.size());

        em.getTransaction().begin();
        collections = dao.getVirtualCollections(em, 0, 1000, getPrivateQueryFactory(TestDatasetProvider.owner1Name));
        em.getTransaction().commit();
        Assert.assertEquals(TestDatasetProvider.owner1PrivateCollectionCount, collections.size());

        em.getTransaction().begin();
        collections = dao.getVirtualCollections(em, 0, 1000, getPrivateQueryFactory(TestDatasetProvider.owner2Name));
        em.getTransaction().commit();
        Assert.assertEquals(TestDatasetProvider.owner2PrivateCollectionCount, collections.size());
    }

    @Test
    public void getVirtualCollectionsTest() throws VirtualCollectionRegistryException {
        em.getTransaction().begin();
        List<VirtualCollection> collections = dao.getVirtualCollections(em, getPublicQueryFactory());
        em.getTransaction().commit();
        Assert.assertEquals(TestDatasetProvider.publicCollectionCount, collections.size());

        em.getTransaction().begin();
        List<VirtualCollection> collections1 = dao.getVirtualCollections(em, getPrivateQueryFactory(TestDatasetProvider.owner1Name));
        em.getTransaction().commit();
        Assert.assertEquals(TestDatasetProvider.owner1PrivateCollectionCount, collections1.size());
        Assert.assertEquals(TestDatasetProvider.owner1Name, collections1.get(0).getOwner().getName());

        em.getTransaction().begin();
        List<VirtualCollection> collections2 = dao.getVirtualCollections(em, getPrivateQueryFactory(TestDatasetProvider.owner2Name));
        em.getTransaction().commit();
        Assert.assertEquals(TestDatasetProvider.owner2PrivateCollectionCount, collections2.size());
        Assert.assertEquals(TestDatasetProvider.owner2Name, collections2.get(0).getOwner().getName());
    }

    private QueryFactory getPublicQueryFactory() {
        QueryFactory qryFactory = new QueryFactory();
        qryFactory.and(QueryOptions.Property.VC_PUBLIC_LEAF, QueryOptions.Relation.EQ, Boolean.TRUE);
        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC);
        states.add(VirtualCollection.State.PUBLIC_FROZEN);
        qryFactory.and(QueryOptions.Property.VC_STATE, QueryOptions.Relation.IN, states);
        return qryFactory;
    }

    private QueryFactory getPrivateQueryFactory(String owner_name) {
        QueryFactory qryFactory = new QueryFactory();
        qryFactory.andIsNull(QueryOptions.Property.VC_CHILD);
        qryFactory.and(QueryOptions.Property.VC_OWNER, QueryOptions.Relation.EQ, owner_name);
        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC);
        states.add(VirtualCollection.State.PUBLIC_PENDING);
        states.add(VirtualCollection.State.PUBLIC_FROZEN);
        states.add(VirtualCollection.State.PUBLIC_FROZEN_PENDING);
        states.add(VirtualCollection.State.PRIVATE);
        states.add(VirtualCollection.State.ERROR);
        qryFactory.and(QueryOptions.Property.VC_STATE, QueryOptions.Relation.IN, states);
        return qryFactory;
    }
}
