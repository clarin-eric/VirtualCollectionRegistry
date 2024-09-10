package eu.clarin.cmdi.virtualcollectionregistry.gui;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.SingleThemeProvider;
import eu.clarin.cmdi.virtualcollectionregistry.*;
import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.*;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin.AdminPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.CreateAndEditVirtualCollectionPageV2;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission.SubmitVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.LoginPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.LogoutPage;
import eu.clarin.cmdi.wicket.theme.ClarinBootstrap5Theme;
import eu.clarin.cmdi.wicket.ExtremeNoopTheme;
import org.apache.wicket.Page;
import static org.apache.wicket.RuntimeConfigurationType.DEPLOYMENT;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Application extends AuthenticatedWebApplication {

    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public final static StringValidator MAX_LENGTH_VALIDATOR = 
        new StringValidator(null, 255);

    public final static Class HOME_PAGE_CLASS = BrowsePublicCollectionsPage.class;

    @Autowired
    private VirtualCollectionRegistry registry;

    @Autowired
    private DataStore dataStore;

    @Autowired
    private AdminUsersService adminUsersService;

    @Autowired
    private VcrConfig vcrConfig;

    @Autowired
    private PermaLinkService permaLinkService;

    public Application() {}

    public Application(VirtualCollectionRegistry registry, DataStore dataStore, AdminUsersService adminUsersService, VcrConfig vcrConfig, PermaLinkService permaLinkService) {
        this.registry = registry;
        this.dataStore = dataStore;
        this.adminUsersService = adminUsersService;
        this.vcrConfig = vcrConfig;
        this.permaLinkService = permaLinkService;
    }

    @Override
    protected void init() {
        super.init();
             
        //Install bootstrap
        Bootstrap.install(
            this, 
            new BootstrapSettings()
                //bootstrap CSS is provided via markup (CSS link in HTML head)
                //.setThemeProvider(new SingleThemeProvider(new ExtremeNoopTheme()))
                .setThemeProvider(new SingleThemeProvider(new ClarinBootstrap5Theme()))
                //.setJsResourceReference(JavaScriptResources.getBootstrapJS())
                .setAutoAppendResources(true)
                .setUpdateSecurityManager(true)
        );
        
        //FontAwesomeSettings.get(Application.get()).setCssResourceReference(FontAwesome5CssReference.instance());

        //Disable CSP for now 
        //TODO: look into ways of enabling this again, see https://nightlies.apache.org/wicket/guide/9.x/single.html#_content_security_policy_csp
        getCspSettings().blocking().disabled();
    
        logger.info("Initialising VCR web application");
        if (vcrConfig != null) {
            vcrConfig.logConfig(); //write current configuration to logger
        } else {
            logger.error("Failed to inject VcrConfig");
            throw new RuntimeException("Failed to start the Virtual Collection Registry. Failed to inject VCR configuration");
        }

        initSpring();

        getApplicationSettings().setPageExpiredErrorPage(HOME_PAGE_CLASS);
        getMarkupSettings().setDefaultMarkupEncoding("utf-8");
        getRequestCycleSettings().setResponseRequestEncoding("utf-8");

        if (!DEPLOYMENT.equals(getConfigurationType())) {
            logger.warn("Web application configured for development");
            getMarkupSettings().setStripWicketTags(true);
            getMarkupSettings().setStripComments(true);
        }

        mountPage("/login", LoginPage.class);
        mountPage("/logout", LogoutPage.class);
        mountPage("/public", BrowsePublicCollectionsPage.class);
        mountPage("/private", BrowsePrivateCollectionsPage.class);
        mountPage("/create", CreateAndEditVirtualCollectionPageV2.class);
        mountPage("/edit/${collection-id}", CreateAndEditVirtualCollectionPageV2.class);
        mountPage("/about", AboutPage.class);
        mountPage("/help", HelpPage.class);
        mountPage("/admin", AdminPage.class);
        mountPage("/details/${collection-id}", VirtualCollectionDetailsPage.class);
        mountPage("/submit/${type}", SubmitVirtualCollectionPage.class);
        mountPage("/profile", UserProfilePage.class);
    }

    protected void initSpring() {
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HOME_PAGE_CLASS;
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

    public VcrConfig getConfig() {
        return vcrConfig;
    }

    public PermaLinkService getPermaLinkService() { return permaLinkService; }
    
} // class Application
