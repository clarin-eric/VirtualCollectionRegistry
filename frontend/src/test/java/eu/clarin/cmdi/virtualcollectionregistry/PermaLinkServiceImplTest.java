package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionFactory;
import org.junit.Assert;
import org.junit.Test;

public class PermaLinkServiceImplTest {

    private final String baseUri = "http://localhost/vcr";
    private final PermaLinkService service = new PermaLinkServiceImpl(baseUri);

    @Test
    public void testGetBaseUri() {
        Assert.assertEquals(baseUri, service.getBaseUri());
    }

    @Test
    public void testGetCollectionUrl() {
        VirtualCollection collection = VirtualCollectionFactory.createNew(new User("test"), "test", "test").getCollection();
        collection.setId(1L);
        Assert.assertEquals(String.format("%s/service/virtualcollections/1", baseUri), service.getCollectionUrl(collection));
    }

    @Test(expected = RuntimeException.class)
    public void testGetNullCollectionUrl() {
        VirtualCollection collection = null;
        Assert.assertEquals(String.format("%s/service/virtualcollections/1", baseUri), service.getCollectionUrl(collection));
    }

    @Test
    public void testGetCollectionUrlById() {
        Long id = 1L;
        Assert.assertEquals(String.format("%s/service/virtualcollections/1", baseUri), service.getCollectionUrl(id));
    }

    @Test(expected = RuntimeException.class)
    public void testGetCollectionUrlByNullId() {
        Long id = null;
        Assert.assertEquals(String.format("%s/service/virtualcollections/1", baseUri), service.getCollectionUrl(id));
    }

    @Test
    public void testGetCollectionDetailsUrl() {
        VirtualCollection collection = VirtualCollectionFactory.createNew(new User("test"), "test", "test").getCollection();
        collection.setId(1L);
        Assert.assertEquals(String.format("%s/details/1", baseUri), service.getCollectionDetailsUrl(collection));
    }

    @Test(expected = RuntimeException.class)
    public void testGetNullCollectionDetailsUrl() {
        VirtualCollection collection = null;
        Assert.assertEquals(String.format("%s/details/1", baseUri), service.getCollectionDetailsUrl(collection));
    }

    @Test
    public void testGetCollectionDetailsUrlById() {
        Long id = 1L;
        Assert.assertEquals(String.format("%s/details/1", baseUri), service.getCollectionDetailsUrl(id));
    }

    @Test(expected = RuntimeException.class)
    public void testGetCollectionDetailsUrlByNullId() {
        Long id = null;
        Assert.assertEquals(String.format("%s/details/1", baseUri), service.getCollectionDetailsUrl(id));
    }
}
