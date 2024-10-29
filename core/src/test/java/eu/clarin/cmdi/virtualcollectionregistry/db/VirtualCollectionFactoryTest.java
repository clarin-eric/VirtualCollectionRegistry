package eu.clarin.cmdi.virtualcollectionregistry.db;

import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionFactory;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VirtualCollectionFactoryTest {

    private DbEnvironment db;

    @Before
    public void prepareDb() throws Exception {
        this.db = DbEnvironment.initialize();
    }

    @After
    public void cleanupDb() throws Exception {
        db.destroy();
    }

    @Test
    public void testPersist() throws VirtualCollectionRegistryException, Exception {
        User owner = new User("test", "Test user");
        VirtualCollection vc =
                VirtualCollectionFactory
                        .createNew(owner, "Collection name", "Collection description")
                        .persist(db.getVirtualCollectionDao())
                        .getCollection();

        Assert.assertTrue("Collection must not be null", vc != null);
        Assert.assertTrue("Collection must have an id", vc.getId() != null);
        Assert.assertTrue("Collection must have a root", vc.getRoot() != null);
        Assert.assertTrue("Collection must have a state", vc.getState() != null);
        Assert.assertTrue("Collection must be in private state", vc.getState() == VirtualCollection.State.PRIVATE);
        Assert.assertTrue("Collection must be public", vc.isPrivate());
        Assert.assertTrue("Not forked, must not have a fork", vc.getForkedFrom() == null);
        Assert.assertTrue("Not versioned, collection root id must be equal to collection id", vc.getRoot().getId() == vc.getId());
        Assert.assertTrue("Not versioned, must not have a parent", vc.getParent() == null);
        Assert.assertTrue("Not versioned, must not have a child", vc.getChild() == null);
    }

    @Test
    public void testPublish() throws VirtualCollectionRegistryException, Exception {
        User owner = new User("test", "Test user");
        VirtualCollection vc =
                VirtualCollectionFactory
                        .createNew(owner, "Collection name", "Collection description")
                        .startPublish(db.getVirtualCollectionDao())
                        .finishPublish(db.getVirtualCollectionDao())
                        .getPersistedCollection(db.getVirtualCollectionDao());

        Assert.assertNotNull("Collection must not be null", vc);
        Assert.assertNotNull("Collection must have an id", vc.getId());
        Assert.assertNotNull("Collection must have a root", vc.getRoot());
        Assert.assertNotNull("Collection must have a state", vc.getState());
        Assert.assertEquals("Collection must be in public state", VirtualCollection.State.PUBLIC, vc.getState());
        Assert.assertTrue("Collection must be public", vc.isPublic());
        Assert.assertTrue("Collection must be public leaf", vc.isPublicLeaf());
        Assert.assertNull("Not forked, must not have a fork", vc.getForkedFrom());
        Assert.assertNull("Not versioned, must not have a parent", vc.getParent());
        Assert.assertNull("Not versioned, must not have a child", vc.getChild());
    }

    @Test
    public void testForking() throws VirtualCollectionRegistryException, Exception {
        User owner = new User("test", "Test user");
        VirtualCollection vc =
                VirtualCollectionFactory
                        .createNew(owner, "Collection name", "Collection description")
                        .startPublish(db.getVirtualCollectionDao())
                        .finishPublish(db.getVirtualCollectionDao())
                        .setState(VirtualCollection.State.PUBLIC)
                        .getPersistedCollection(db.getVirtualCollectionDao());

        Assert.assertNotNull("Persisted collection must have an id", vc.getId());
        Assert.assertEquals("Collection must be in public state", VirtualCollection.State.PUBLIC, vc.getState());

         User owner2 = new User("test 2", "Test user 2");
        VirtualCollection vcForked =
                VirtualCollectionFactory
                        .createFork(vc, owner2)
                        .getPersistedCollection(db.getVirtualCollectionDao());

        Assert.assertNotNull("Collection must have an id after persist", vcForked.getId());
        Assert.assertTrue("Collection must have a value id (>= 0) after persist", vcForked.getId() >= 0);
        Assert.assertTrue("Collection must be private", vcForked.isPrivate());
        Assert.assertNotNull("Forked, must have a fork", vcForked.getForkedFrom());
        Assert.assertEquals("Forked, forked from id must match vc root id", vc.getId(), vcForked.getForkedFrom().getId());
        Assert.assertNotNull("Must have an owner", vcForked.getOwner());
        Assert.assertTrue("Must have an owner with name matching owner2 name", vcForked.getOwner().getName().equalsIgnoreCase(owner2.getName()));
        Assert.assertEquals("Not versioned, collection root id must be equal to collection id", vcForked.getId(), vcForked.getRoot().getId());
        Assert.assertNull("Not versioned, must not have a parent", vcForked.getParent());
        Assert.assertNull("Not versioned, must not have a child", vcForked.getChild());
    }

    @Test
    public void testVersioning() throws VirtualCollectionRegistryException, Exception {
        //Create first collection
        User owner = new User("test", "Test user");
        VirtualCollection vcRoot =
                VirtualCollectionFactory
                        .createNew(owner, "Collection name", "Collection description")
                        .startPublish(db.getVirtualCollectionDao())
                        .finishPublish(db.getVirtualCollectionDao())
                        .setState(VirtualCollection.State.PUBLIC)
                        .getPersistedCollection(db.getVirtualCollectionDao());

        Assert.assertNotNull("Persisted collection must have an id", vcRoot.getId());
        Assert.assertEquals("Collection must be in public state", VirtualCollection.State.PUBLIC, vcRoot.getState());
        Assert.assertTrue("Collection must be a public leaf", vcRoot.isPublicLeaf());
/*
        TODO resolve unique constraint violation
        
        //Create a new collection, private at first, published later
        VirtualCollection vcNewVersion =
            VirtualCollectionFactory
                .createNewVersion(vcRoot)
                .getPersistedCollection(db.getVirtualCollectionDao());

        Assert.assertTrue("Collection must be private", vcNewVersion.isPrivate());
        Assert.assertNull("Not forked, must not have a fork", vcNewVersion.getForkedFrom());
        Assert.assertEquals("New version, collection root id must be equal to root id", vcRoot.getId(), vcNewVersion.getRoot().getId());
        Assert.assertNotNull("New version, must have a parent", vcNewVersion.getParent());
        Assert.assertEquals("New version, parent id must match root vc id", vcRoot.getId(), vcNewVersion.getParent().getId());
        Assert.assertFalse("Collection must not be a public leaf", vcNewVersion.isPublicLeaf());
        Assert.assertNull("New version, private leaf must not have a child", vcNewVersion.getChild());

        VirtualCollectionFactory vcf = VirtualCollectionFactory
                .fromExisting(vcNewVersion)
                .startPublish(db.getVirtualCollectionDao());
        Assert.assertEquals("Collection must be in public_pending state", VirtualCollection.State.PUBLIC_PENDING, vcNewVersion.getState());

        vcf.finishPublish(db.getVirtualCollectionDao());
        Assert.assertEquals("Collection must be in public state", VirtualCollection.State.PUBLIC, vcNewVersion.getState());
        Assert.assertTrue("Collection must not be a public leaf", vcNewVersion.isPublicLeaf());
        Assert.assertFalse("Parent collection must still be a public leaf", vcRoot.isPublicLeaf());

        VirtualCollection vcRootFromDb = db.getVirtualCollectionDao().getVirtualCollection(vcRoot.getId());
        Assert.assertFalse("Parent collection retrieved from db must not be a public leaf anymore", vcRootFromDb.isPublicLeaf());

        //Create a third version
        VirtualCollection vcAnotherNewVersion =
                VirtualCollectionFactory
                        .createNewVersion(vcNewVersion)
                        .getPersistedCollection(db.getVirtualCollectionDao());

        Assert.assertTrue("Collection must be private", vcAnotherNewVersion.isPrivate());
        Assert.assertNull("Not forked, must not have a fork", vcAnotherNewVersion.getForkedFrom());
        Assert.assertEquals("New version, collection root id must be equal to root id", vcRoot.getId(), vcAnotherNewVersion.getRoot().getId());
        Assert.assertNotNull("New version, must have a parent", vcAnotherNewVersion.getParent());
        Assert.assertEquals("New version, parent id must match previous version vc id", vcNewVersion.getId(), vcAnotherNewVersion.getParent().getId());
        Assert.assertFalse("Collection must not be a public leaf", vcAnotherNewVersion.isPublicLeaf());
        Assert.assertNull("New version, private leaf must not have a child", vcAnotherNewVersion.getChild());
*/
    }

    @Test(expected = IllegalStateException.class)
    public void testVersioningFromPrivateCollection() throws VirtualCollectionRegistryException {
        //Create private collection
        User owner = new User("test", "Test user");
        VirtualCollection vcRoot =
                VirtualCollectionFactory
                        .createNew(owner, "Collection name", "Collection description")
                        .getPersistedCollection(db.getVirtualCollectionDao());

        Assert.assertNotNull("Persisted collection must have an id", vcRoot.getId());
        Assert.assertTrue("Collection must be private", vcRoot.isPrivate());

        //Try to create a new version. This shoudl fail since the parent is private
        VirtualCollection vcNewVersion =
                VirtualCollectionFactory
                        .createNewVersion(vcRoot)
                        .getCollection();
    }
}
