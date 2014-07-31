package eu.clarin.cmdi.virtualcollectionregistry.gui;

import eu.clarin.cmdi.virtualcollectionregistry.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.AdminPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowsePrivateCollectionsPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowsePublicCollectionsPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.CreateVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.EditVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.LoginPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.VirtualCollectionDetailsPage;
import org.apache.wicket.Page;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.MixedParamHybridUrlCodingStrategy;
import org.apache.wicket.session.pagemap.LeastRecentlyAccessedEvictionStrategy;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Application extends AuthenticatedWebApplication {

    private final static Logger logger = LoggerFactory.getLogger(Application.class);
    
    @Autowired
    private VirtualCollectionRegistry registry;
    @Autowired
    private DataStore dataStore;
    @Autowired
    private AdminUsersService adminUsersService;

    @Override
    protected void init() {
        super.init();
        logger.info("Initialising VCR web application");
        addComponentInstantiationListener(new SpringComponentInjector(this));

        getMarkupSettings().setDefaultMarkupEncoding("utf-8");
        getRequestCycleSettings().setResponseRequestEncoding("utf-8");
        getSessionSettings().setMaxPageMaps(3);
        getSessionSettings().setPageMapEvictionStrategy(
                new LeastRecentlyAccessedEvictionStrategy(3));
        if (!DEPLOYMENT.equals(getConfigurationType())) {
            logger.warn("Web application configured for development");
            getMarkupSettings().setStripWicketTags(true);
            getMarkupSettings().setStripComments(true);
        }

        mountBookmarkablePage("/login",
                LoginPage.class);
        mountBookmarkablePage("/public",
                BrowsePublicCollectionsPage.class);
        mountBookmarkablePage("/private",
                BrowsePrivateCollectionsPage.class);
        mountBookmarkablePage("/create", CreateVirtualCollectionPage.class);
        mountBookmarkablePage("/admin", AdminPage.class);

        // details of an existing collection by ID, e.g. /details/123
        mount(new MixedParamHybridUrlCodingStrategy("/details",
                VirtualCollectionDetailsPage.class, new String[]{VirtualCollectionDetailsPage.PARAM_VC_ID}));
        // editing an existing collection by ID, e.g. /edit/123
        mount(new MixedParamHybridUrlCodingStrategy("/edit",
                EditVirtualCollectionPage.class, new String[]{"id"}));
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return BrowsePublicCollectionsPage.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return LoginPage.class;
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return ApplicationSession.class;
    }

    public boolean hasAnyRole(String[] roles) {
        if (roles != null) {
            final Roles sessionRoles = AuthenticatedWebSession.get().getRoles();
            if (sessionRoles != null) {
                for (String role : roles) {
                    if (sessionRoles.hasRole(role)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    boolean isAdmin(String user) {
        return adminUsersService.isAdmin(user);
    }
    
    public VirtualCollectionRegistry getRegistry() {
        return registry;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public static Application get() {
        return (Application) WebApplication.get();
    }

} // class Application
