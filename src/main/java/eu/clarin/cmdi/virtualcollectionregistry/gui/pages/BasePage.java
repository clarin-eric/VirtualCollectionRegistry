package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.INavbarComponent;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.ImmutableNavbarComponent;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar.ComponentPosition;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarExternalLink;
import eu.clarin.cmdi.virtualcollectionregistry.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.config.PiwikConfigImpl;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasePage extends WebPage {

    private static Logger logger = LoggerFactory.getLogger(BasePage.class);
    
    @SpringBean
    private AdminUsersService adminUsersService;

    @SpringBean
    private PiwikConfigImpl piwikConfig;
    
    public static final String BETA_MODE = "eu.clarin.cmdi.virtualcollectionregistry.beta_mode";
    
    private final static JavaScriptResourceReference INIT_JAVASCRIPT_REFERENCE = new JavaScriptResourceReference(BasePage.class, "BasePage.js");
    
    protected BasePage(IModel<?> model) {
        super(model);
        addComponents();
    }
    
    protected BasePage() {
        addComponents();
    }

     @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        //TODO: check if https://ci.apache.org/projects/wicket/apidocs/6.x/org/apache/wicket/markup/head/OnLoadHeaderItem.html is a better alternative
        response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));  //Ensure jquery is loaded before custom script
        response.render(JavaScriptReferenceHeaderItem.forReference(INIT_JAVASCRIPT_REFERENCE));
    }
    
    private void addComponents() {
        final boolean beta_mode = Boolean.valueOf(WebApplication.get().getServletContext().getInitParameter(BETA_MODE));

        // add Navbar
        add(new WebMarkupContainer("header")
                .add(createHeaderMenu("menu"))); // navbar in header

        // Add feedback panel to show information and error messages
        add(new FeedbackPanel("feedback"));
        
        // add Piwik tracker (if enabled)
        if (piwikConfig.isEnabled()) {
            add(new PiwikTracker("piwik", piwikConfig.getSiteId(), piwikConfig.getPiwikHost(), piwikConfig.getDomains()));
        } else {
            add(new WebMarkupContainer("piwik")); //empty placeholder
        }
    }
    
    private Component createHeaderMenu(String id) {
        final Navbar navbar = new Navbar(id) {
            @Override
            protected Label newBrandLabel(String markupId) {
                //set label to not escape model strings to allow HTML
                return (Label) super.newBrandLabel(markupId).setEscapeModelStrings(false);
            }

        };
        navbar.setBrandName(Model.of("<i class=\"glyphicon glyphicon-book\" aria-hidden=\"true\"></i> Virtual Collection Registry"));
        
        final List<INavbarComponent> menuItems = new ArrayList<>();
        //Default menu items
        menuItems.add(new ImmutableNavbarComponent(new NavbarButton(BrowsePublicCollectionsPage.class, Model.of("Browse")), ComponentPosition.LEFT));
        //menuItems.add(new ImmutableNavbarComponent(new NavbarButton(BrowsePrivateCollectionsPage.class, Model.of("My Collections")), ComponentPosition.LEFT));
        menuItems.add(new ImmutableNavbarComponent(new NavbarButton(CreateVirtualCollectionPageSimple.class, Model.of("Create")), ComponentPosition.LEFT));
        menuItems.add(new ImmutableNavbarComponent(new NavbarButton(HelpPage.class, Model.of("Help")), ComponentPosition.LEFT));
        
        if (isSignedIn() && isUserAdmin()) {
            menuItems.add(new ImmutableNavbarComponent(new NavbarButton(AdminPage.class, Model.of("Admin")), ComponentPosition.LEFT));
        }
        
        //Add login or user profile + logout buttons based on authentication state
        if(isSignedIn()) {
            final Component userLink = new NavbarButton(BrowsePrivateCollectionsPage.class, Model.of(getUser().getName()))
                    .add(new AttributeModifier("class", "glyphicon glyphicon-user"));
            final Component logoutLink = new NavbarButton(LogoutPage.class, Model.of("Logout"))
                .add(new AttributeModifier("class", "glyphicon glyphicon-log-out"));
        
            
            menuItems.add(new ImmutableNavbarComponent(userLink, ComponentPosition.RIGHT));
            menuItems.add(new ImmutableNavbarComponent(logoutLink, ComponentPosition.RIGHT));
        } else {
            final Component loginLink = new NavbarButton(LoginPage.class, Model.of("Login"))
                .add(new AttributeModifier("class", "glyphicon glyphicon-log-in"));
            menuItems.add(new ImmutableNavbarComponent(loginLink, ComponentPosition.RIGHT));
        }
        // link to CLARIN website
        final Component clarinLink = new NavbarExternalLink(Model.of("http://www.clarin.eu/")) {
            @Override
            protected Component newLabel(String markupId) {
                return super.newLabel(markupId).setEscapeModelStrings(false);
            }

        }
            .setLabel(Model.of("<span>CLARIN</span>"))
            .add(new AttributeModifier("class", "clarin-logo hidden-xs"));
        menuItems.add(new ImmutableNavbarComponent(clarinLink, ComponentPosition.RIGHT));

        navbar.addComponents(menuItems);
        return navbar;
    }

    
    @Override
    protected void onBeforeRender() {
        // skip lazy auto-auth for login page
        if (!this.getClass().isInstance(LoginPage.class)) {
            final RequestCycle cycle =  RequestCycle.get();
            final HttpServletRequest request = 
                ((ServletWebRequest)cycle.getRequest()).getContainerRequest();
            final ApplicationSession session = getSession();
            if (!session.isSignedIn()) {
                if (request.getAuthType() != null) {
                    // FIXME: better logging
                    System.err.println("Auth, but no authed session -> login");
                    final Principal principal = request.getUserPrincipal();
                    if (!session.signIn(principal)) {
                        throw new RestartResponseException(getApplication()
                                .getApplicationSettings()
                                .getAccessDeniedPage());
                    }
                }
            } else {
                if (request.getAuthType() == null) {
                    // FIXME: better logging
                    System.err.println("Lost Session!");
                    session.invalidate();
                    throw new RestartResponseException(getApplication()
                            .getApplicationSettings()
                            .getPageExpiredErrorPage());
                }
            }
        }
        super.onBeforeRender();
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new WebComponent("canonicalUrl") {
            @Override
            protected void onRender() {
                final IModel<String> canonicalUrlModel = getCanonicalUrlModel();
                if (canonicalUrlModel != null) {
                    getResponse().write("<link rel=\"canonical\" href=\"" + canonicalUrlModel.getObject() + "\"/>");
                }
            }

        });
    }

    protected boolean isSignedIn() {
        return ((AuthenticatedWebSession) getSession()).isSignedIn() && getSession().getPrincipal() != null;
    }
    
    protected Principal getUser() {
        Principal principal = getSession().getPrincipal();
        if (principal == null) {
            throw new WicketRuntimeException("principal == null");
        }
        return principal;
    }

    protected boolean isUserAdmin() {
        try {
            final String userName = getUser().getName();
            return userName != null && adminUsersService.isAdmin(userName);
        } catch(WicketRuntimeException ex) {
            logger.error("Invalid principal", ex);
            return false;
        }
    }

    @Override
    public ApplicationSession getSession() {
        return (ApplicationSession) super.getSession();
    }
    
    /**
     *
     * @return URL to include as a canonical HREF in the page header.
     */
    public IModel<String> getCanonicalUrlModel() {
        //return null;
        final CharSequence url = RequestCycle.get().urlFor(getClass(), null);
        final String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
        return new Model(absoluteUrl);
    
    }

} // class BasePage
