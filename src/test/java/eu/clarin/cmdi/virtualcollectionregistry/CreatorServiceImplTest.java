package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CreatorServiceImplTest {

    private final CreatorServiceImpl service = new CreatorServiceImpl();

    public CreatorServiceImplTest() {
        List<VirtualCollection> collections = new ArrayList<>();
        collections.add(
            VirtualCollectionFactory
                .createNew(new User("test user"), "Test", "Test")
                .addCreator(new Creator("creatorFamilyName", "creatorGivenName"))
                .publish()
                .getCollection()
        );
        service.initialize(collections);
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
