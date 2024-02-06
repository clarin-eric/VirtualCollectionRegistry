package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfigImpl;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;

public class TestApplication extends Application  {
    public static final String BASE_URI = "http://vcr";

    public TestApplication() {
        this(
            new VirtualCollectionRegistryImpl(null),
            null, //new DataStore(),
            new AdminUsersServiceImpl(),
            new VcrConfigImpl(),
            new PermaLinkServiceImpl(BASE_URI)
        );
    }

    public TestApplication(VirtualCollectionRegistry registry, DataStore dataStore, AdminUsersService adminUsersService, VcrConfig vcrConfig, PermaLinkService permaLinkService) {
        super(registry, dataStore, adminUsersService, vcrConfig, permaLinkService);
    }

    @Override
    protected void initSpring() {
        //Do nothing, disabling spring injection
    }
}
