package eu.clarin.cmdi.virtualcollectionregistry.core;

import eu.clarin.cmdi.virtualcollectionregistry.db.DbEnvironment;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CreatorServiceImplTest {

    private final CreatorServiceImpl service = new CreatorServiceImpl();

    public CreatorServiceImplTest() throws Exception {
        DbEnvironment db = DbEnvironment.initialize();
        List<VirtualCollection> collections = new ArrayList<>();
        collections.add(
            VirtualCollectionFactory
                .createNew(new User("test user"), "Test", "Test")
                .addCreator(new Creator("creatorFamilyName", "creatorGivenName"))
                .startPublish(db.getVirtualCollectionDao())
                .finishPublish(db.getVirtualCollectionDao())
                .getCollection()
        );
        service.initialize(collections);
        db.destroy();
    }

    @Test
    public void testIsInitialized() {
        Assert.assertEquals(true, service.isInitialized());
    }

    @Test
    public void testNullInitialize() {
        CreatorServiceImpl s = new CreatorServiceImpl();
        s.initialize(null);
        Assert.assertEquals(false, s.isInitialized());
    }

    @Test
    public void testEmptyInitialize() {
        List<VirtualCollection> collections = new ArrayList<>();
        new CreatorServiceImpl().initialize(collections);
    }

    @Test
    public void testGetCreators() {
        String currentPrincipal = "current_user.nl@clarin.eu";
        Assert.assertEquals(2, service.getCreators(currentPrincipal).size());
    }

    @Test
    public void testGetSize() {
        Assert.assertEquals(1, service.getSize());
    }
}
