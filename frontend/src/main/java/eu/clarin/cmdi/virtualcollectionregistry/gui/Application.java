package eu.clarin.cmdi.virtualcollectionregistry.gui;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.SingleThemeProvider;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome6CssReference;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeSettings;
import eu.clarin.cmdi.virtualcollectionregistry.core.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.core.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.*;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin.AdminPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.CreateAndEditVirtualCollectionPageV2;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission.SubmitVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.LoginPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.LogoutPage;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import eu.clarin.cmdi.wicket.theme.ClarinBootstrap5Theme;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import static org.apache.wicket.RuntimeConfigurationType.DEPLOYMENT;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.filter.FilteredHeaderItem;
import org.apache.wicket.markup.head.filter.FilteringHeaderResponse;
import org.apache.wicket.markup.head.filter.FilteringHeaderResponse.IHeaderResponseFilter;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
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
    private AdminUsersService adminUsersService;

    @Autowired
    private VcrConfig vcrConfig;

    @Autowired
    private PermaLinkService permaLinkService;

    public Application() {}

    public Application(VirtualCollectionRegistry registry, AdminUsersService adminUsersService, VcrConfig vcrConfig, PermaLinkService permaLinkService) {
        this.registry = registry;
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
        
        FontAwesomeSettings
            .get(Application.get())
            .setCssResourceReference(FontAwesome6CssReference.instance());

        //Disable CSP for now 
        //TODO: look into ways of enabling this again, see https://nightlies.apache.org/wicket/guide/9.x/single.html#_content_security_policy_csp
        getCspSettings().blocking().disabled();
        //Currently missing clipboard js and switchboard popup in theme
   
        logger.info("Initialising VCR web application");
        if (vcrConfig != null) {
            logger.info(vcrConfig.logConfig()); //write current configuration to logger
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
            getDebugSettings().setAjaxDebugModeEnabled(true);
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
        
        //Listen for AJAX events and make sure to update the feedbackpanel on any
        //AJAX event that set a message.
        getAjaxRequestTargetListeners().add(new AjaxRequestTarget.IListener() {           
            @Override
            public void onBeforeRespond(Map<String, org.apache.wicket.Component> map, AjaxRequestTarget target) {
                Page page = target.getPage();
                if (page instanceof BasePage) {
                    BasePage basePage = (BasePage)page;
                    if (basePage.getFeedbackPanel().anyMessage()) {
                        target.add(basePage.getFeedbackPanel());
                    }
                }
            }
        });
/*
        // This decorator intercepts header items and redirects those 
        // marked "footer-container" to a specific spot in your HTML.
        getHeaderResponseDecorators().add(response -> 
            new JavaScriptFilteredIntoFooterHeaderResponse(response, BasePage.SCRIPT_CONTAINER_ID)
        );
        */
        
        getHeaderResponseDecorators().add(response -> {
            IHeaderResponseFilter myFooterFilter = new IHeaderResponseFilter() {
                @Override
                public String getName() {
                    return BasePage.CSL_FILTER_NAME;
                }

                @Override
                public boolean accepts(HeaderItem item) {
                    // Only intercept if the item is explicitly wrapped in a FilteredHeaderItem
                    // with the matching filter name.
                    if (item instanceof FilteredHeaderItem) {
                        return ((FilteredHeaderItem) item).getFilterName().equals(getName());
                    }
                    return false;
                }
            };

            // This response will handle the "head" by default and your custom filter for the rest
            return new FilteringHeaderResponse(response, "head", Collections.singletonList(myFooterFilter));
        });
    }
    
    @Override
    public RuntimeConfigurationType getConfigurationType() {
        return RuntimeConfigurationType.DEVELOPMENT; // Force development mode for testing
    }

    @Override
    protected void onDestroy() {}

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
        ApplicationSession session = (ApplicationSession)AuthenticatedWebSession.get();
        Principal principal = session.getPrincipal();
        registry.setAuthenticatedUser(principal);
        return registry;
    }

    public static Application get() {
        return (Application) WebApplication.get();
    }

    public VcrConfig getConfig() {
        return vcrConfig;
    }

    public PermaLinkService getPermaLinkService() { return permaLinkService; }


} // class Application
