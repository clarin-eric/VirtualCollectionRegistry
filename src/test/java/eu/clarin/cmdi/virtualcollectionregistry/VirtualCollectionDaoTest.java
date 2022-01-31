package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.*;

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

    private final VirtualCollectionDao[] daos = new VirtualCollectionDao[]{
            //new VirtualCollectionDaoImplNamedQuery(),
            new VirtualCollectionDaoImpl()
    };

    private static String owner1Name = "test1";
    private static String owner2Name = "test2";
    private static void initData() {
        try {
            em.getTransaction().begin();

            User owner1 = new User(owner1Name, "Display as tést owner 1");
            User owner2 = new User(owner2Name, "Display as tést owner 2");

            //private, owner1
            VirtualCollection vc1 =
                    VirtualCollectionFactory
                            .createNew(owner1, "Private Tést Collection 1", "This is the tést collection description")
                            .getCollection();
            em.persist(vc1);

            //public, owner1
            VirtualCollection vc2 =
                    VirtualCollectionFactory
                            .createNew(owner1, "Public Tést Collection 2", "This is the tést collection description")
                            .publish()
                            .getCollection();
            em.persist(vc2);

            //public, owner2
            VirtualCollection vc3 =
                    VirtualCollectionFactory
                            .createNew(owner2, "Public Tést Collection 3", "This is the tést collection description")
                            .publish()
                            .getCollection();
            em.persist(vc3);

            //public, owner2
            VirtualCollection vc4 = VirtualCollectionFactory.fromExisting(vc3).getNewCollectionVersion().publish().getCollection();
            em.persist(vc4);

            //private, owner2
            VirtualCollection vc5 = VirtualCollectionFactory.fromExisting(vc4).getNewCollectionVersion().getCollection();
            em.persist(vc5);
        } finally {
            em.getTransaction().commit();
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        logger.info("Starting in-memory HSQL database for unit tests");
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");

        logger.info("Building JPA EntityManager for unit tests");
        emFactory = Persistence.createEntityManagerFactory("VirtualCollectionStoreTest");
        em = emFactory.createEntityManager();

        logger.info("Initialising database with data");
        initData();
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
    public void getVirtualCollectionsCountTest() throws VirtualCollectionRegistryException {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        for(VirtualCollectionDao dao : daos) {
            em.getTransaction().begin();
            int count = dao.getVirtualCollectionCount(em, getPublicQueryFactory());
            em.getTransaction().commit();

            Assert.assertEquals(
                "Get virtualCollection count (dao="+dao.getClass().toString()+")",
                3, count);
        }
    }

    @Test
    public void getVirtualCollectionsWithOffset() throws VirtualCollectionRegistryException {
        for(VirtualCollectionDao dao : daos) {
            em.getTransaction().begin();
            List<VirtualCollection> collections = dao.getVirtualCollections(em, 0, 0, getPublicQueryFactory());
            em.getTransaction().commit();

            Assert.assertEquals(
                "Get virtualCollection count with offset (dao="+dao.getClass().toString()+")",
                0, collections.size());
        }

        for(VirtualCollectionDao dao : daos) {
            em.getTransaction().begin();
            List<VirtualCollection> collections = dao.getVirtualCollections(em, 0, 1, getPublicQueryFactory());
            em.getTransaction().commit();

            Assert.assertEquals(
                    "Get virtualCollection count with offset (dao="+dao.getClass().toString()+")",
                    1, collections.size());
        }

        for(VirtualCollectionDao dao : daos) {
            em.getTransaction().begin();
            List<VirtualCollection> collections = dao.getVirtualCollections(em, 0, 1000, getPublicQueryFactory());
            em.getTransaction().commit();

            Assert.assertEquals(
                    "Get virtualCollection count with offset (dao="+dao.getClass().toString()+")",
                    3, collections.size());
        }
    }

    @Test
    public void getVirtualCollectionsTest() throws VirtualCollectionRegistryException {
        for(VirtualCollectionDao dao : daos){
            em.getTransaction().begin();
            List<VirtualCollection> collections = dao.getVirtualCollections(em, getPublicQueryFactory());
            em.getTransaction().commit();

            Assert.assertEquals(
                "Get virtualCollections (dao="+dao.getClass().toString()+")",
                3, collections.size());
        }

        for(VirtualCollectionDao dao : daos){
            em.getTransaction().begin();
            List<VirtualCollection> collections1 = dao.getVirtualCollections(em, getPrivateQueryFactory(owner1Name));
            em.getTransaction().commit();

            Assert.assertEquals(
                    "Get virtualCollections (dao="+dao.getClass().toString()+")",
                    2, collections1.size());
            Assert.assertEquals(
                    "Collection owner name",
                    owner1Name, collections1.get(0).getOwner().getName());

            em.getTransaction().begin();
            List<VirtualCollection> collections2 = dao.getVirtualCollections(em, getPrivateQueryFactory(owner2Name));
            em.getTransaction().commit();

            Assert.assertEquals(
                    "Get virtualCollections (dao="+dao.getClass().toString()+")",
                    3, collections2.size());
            Assert.assertEquals(
                    "Collection owner name",
                    owner2Name, collections2.get(0).getOwner().getName());
        }
    }

    private QueryFactory getPublicQueryFactory() {
        QueryFactory qryFactory = new QueryFactory();
        qryFactory.andIsNull(QueryOptions.Property.VC_CHILD);
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
