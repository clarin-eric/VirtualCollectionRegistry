package eu.clarin.cmdi.virtualcollectionregistry.db;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryOptions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class VirtualCollectionDaoTest extends AbstractVirtualCollectionDaoTest {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionDaoTest.class);

    @Test
    public void getPublicVirtualCollectionsCountTest() throws VirtualCollectionRegistryException {
        int count = dao.getVirtualCollectionCount(getPublicQueryFactory());
        Assert.assertEquals(TestDatasetProvider.publicCollectionCount, count);
    }

    @Test
    public void getPrivateVirtualCollectionsCountTest() throws VirtualCollectionRegistryException {
        int count = dao.getVirtualCollectionCount(getPrivateQueryFactory(TestDatasetProvider.owner1Name));
        Assert.assertEquals(TestDatasetProvider.owner1PrivateCollectionCount, count);

        count = dao.getVirtualCollectionCount(getPrivateQueryFactory(TestDatasetProvider.owner2Name));
        Assert.assertEquals(TestDatasetProvider.owner2PrivateCollectionCount, count);
    }

    @Test
    public void getVirtualCollectionsWithOffset() throws VirtualCollectionRegistryException {
        List<VirtualCollection> collections = dao.getVirtualCollections(0, 0, getPublicQueryFactory());
        Assert.assertEquals(0, collections.size());

        collections = dao.getVirtualCollections(0, 1, getPublicQueryFactory());
        Assert.assertEquals(1, collections.size());

        collections = dao.getVirtualCollections(0, 1000, getPublicQueryFactory());
        Assert.assertEquals(TestDatasetProvider.publicCollectionCount, collections.size());

        collections = dao.getVirtualCollections(0, 1000, getPrivateQueryFactory(TestDatasetProvider.owner1Name));
        Assert.assertEquals(TestDatasetProvider.owner1PrivateCollectionCount, collections.size());

        collections = dao.getVirtualCollections(0, 1000, getPrivateQueryFactory(TestDatasetProvider.owner2Name));
        Assert.assertEquals(TestDatasetProvider.owner2PrivateCollectionCount, collections.size());
    }

    @Test
    public void getVirtualCollectionsTest() throws VirtualCollectionRegistryException {
        List<VirtualCollection> collections = dao.getVirtualCollections(getPublicQueryFactory());
        for(VirtualCollection c : collections) {
            logger.info("Collection: id="+c.getId()+", name="+c.getName()+", state="+c.getState().toString()+", public_leaf="+c.isPublicLeaf());
        }
        Assert.assertEquals(TestDatasetProvider.publicCollectionCount, collections.size());

        List<VirtualCollection> collections1 = dao.getVirtualCollections(getPrivateQueryFactory(TestDatasetProvider.owner1Name));
        Assert.assertEquals(TestDatasetProvider.owner1PrivateCollectionCount, collections1.size());
        Assert.assertEquals(TestDatasetProvider.owner1Name, collections1.get(0).getOwner().getName());

        List<VirtualCollection> collections2 = dao.getVirtualCollections(getPrivateQueryFactory(TestDatasetProvider.owner2Name));
        Assert.assertEquals(TestDatasetProvider.owner2PrivateCollectionCount, collections2.size());
        Assert.assertEquals(TestDatasetProvider.owner2Name, collections2.get(0).getOwner().getName());
    }

    @Test
    public void createAndUpdateTest() throws VirtualCollectionRegistryException {
        VirtualCollection vc = new VirtualCollection();
        vc.setType(VirtualCollection.Type.EXTENSIONAL);
        vc.setName("Test collection");
        vc.setDescription("This is a test to create a collection");
        vc.setOwner(new User("test"));

        Assert.assertNull("Must not have an id", vc.getId());
        dao.persist(vc);
        Assert.assertNotNull("Must have an id", vc.getId());

        vc.setName("Updated test collection");
        dao.persist(vc);

        Assert.assertNotNull("Must have an id", vc.getId());
    }

    public class CreateRunnable implements Runnable {
        Long id = null;

        @Override
        public void run() {
            VirtualCollection vc = new VirtualCollection();
            vc.setType(VirtualCollection.Type.EXTENSIONAL);
            vc.setName("Test collection");
            vc.setDescription("This is a test to create a collection");
            vc.setOwner(new User("test"));

            Assert.assertNull("Must not have an id", vc.getId());
            try {
                getNewDaoWithNewDatastoreInstance().persist(vc);
            } catch(Exception ex) {
                logger.error("Failed to persist", ex);
                Assert.assertFalse("No exception expected", true);
            }
            Assert.assertNotNull("Must have an id", vc.getId());
            id = vc.getId();
        }

        public Long getId() {
            return id;
        }
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
