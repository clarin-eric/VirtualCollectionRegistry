package eu.clarin.cmdi.virtualcollectionregistry.gui;

import eu.clarin.cmdi.virtualcollectionregistry.core.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.core.AdminUsersServiceImpl;
import eu.clarin.cmdi.virtualcollectionregistry.core.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.core.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.core.PermaLinkServiceImpl;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistryImpl;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfigImpl;

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
