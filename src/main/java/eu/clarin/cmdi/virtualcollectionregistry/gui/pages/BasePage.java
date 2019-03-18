package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import de.agilecoders.wicket.core.markup.html.bootstrap.image.GlyphIconType;
import eu.clarin.cmdi.wicket.PiwikTracker;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.LogoutPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.AuthenticationHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.LoginPage;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.INavbarComponent;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.ImmutableNavbarComponent;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar.ComponentPosition;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarExternalLink;
import eu.clarin.cmdi.virtualcollectionregistry.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.IValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.wicket.PiwikConfig;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.include.Include;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasePage extends WebPage {

    private static Logger logger = LoggerFactory.getLogger(BasePage.class);
    
    @SpringBean
    private AdminUsersService adminUsersService;

    @SpringBean
    private PiwikConfig piwikConfig;
    
    @SpringBean
    private VcrConfig vcrConfig;
    
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

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setFilter((FeedbackMessage fm) -> !(fm.getMessage() instanceof IValidationFailedMessage));
        add(feedback);

        // add Piwik tracker (if enabled)
        if (piwikConfig.isEnabled()) {
            add(new PiwikTracker("piwik", piwikConfig.getSiteId(), piwikConfig.getPiwikHost(), piwikConfig.getDomains()));
        } else {
            add(new WebMarkupContainer("piwik")); //empty placeholder
        }
        
        //Include survey if configured (typically mopinion user satisfaction
        if (Strings.isEmpty(piwikConfig.getSnippetSurvey())) {
            add(new WebMarkupContainer("surveySnippet"));
        } else {
            add(new Include("surveySnippet", piwikConfig.getSnippetSurvey()));
        }
         
        //Include extra credits if configured
        if (Strings.isEmpty(piwikConfig.getSnippetCredits())) {
            add(new WebMarkupContainer("creditsSnippet"));
        } else {
            add(new Include("creditsSnippet", piwikConfig.getSnippetCredits()));
        }
        
        String mode = piwikConfig.getMode();
        WebMarkupContainer badge = new WebMarkupContainer("badge");
        badge.setVisible(mode.equalsIgnoreCase("beta") || mode.equalsIgnoreCase("alpha"));
        if (mode.equalsIgnoreCase("beta")) {
            badge.add(new AttributeModifier("class", "vcr-badge beta"));
        } else if(mode.equalsIgnoreCase("alpha")) {
            badge.add(new AttributeModifier("class", "vcr-badge alpha"));
        }
        add(badge);
    }
    
    private Component createHeaderMenu(String id) {
        final Navbar navbar = new Navbar(id) {
            @Override
            protected Label newBrandLabel(String markupId) {
                //set label to not escape model strings to allow HTML
                return (Label) super.newBrandLabel(markupId).setEscapeModelStrings(false);
            }
        };
        navbar.setBrandName(Model.of("<i class=\"glyphicon glyphicon-book\" aria-hidden=\"true\"></i> Virtual Collections"));
        
        final List<INavbarComponent> menuItems = new ArrayList<>();
        //Default menu items
        menuItems.add(new ImmutableNavbarComponent(new NavbarButton(BrowsePublicCollectionsPage.class, Model.of("Browse")), ComponentPosition.LEFT));
        menuItems.add(new ImmutableNavbarComponent(new NavbarButton(CreateAndEditVirtualCollectionPage.class, Model.of("Create")), ComponentPosition.LEFT));
        menuItems.add(new ImmutableNavbarComponent(new NavbarButton(HelpPage.class, Model.of("Help")), ComponentPosition.LEFT));
        
        if (isSignedIn() && isUserAdmin()) {
            menuItems.add(new ImmutableNavbarComponent(new NavbarButton(AdminPage.class, Model.of("Admin")), ComponentPosition.LEFT));
        }
        
        //Add login or user profile + logout buttons based on authentication state
        if(isSignedIn()) {
            final NavbarButton userLink = new NavbarButton(BrowsePrivateCollectionsPage.class, Model.of(getUser().getName()));
            userLink.setIconType(GlyphIconType.user);
            menuItems.add(new ImmutableNavbarComponent(userLink, ComponentPosition.RIGHT));
            
            if(vcrConfig.isLogoutEnabled()) {
            final NavbarButton logoutLink = new NavbarButton(LogoutPage.class, Model.of("Logout"));
            logoutLink.setIconType(GlyphIconType.logout);
            menuItems.add(new ImmutableNavbarComponent(logoutLink, ComponentPosition.RIGHT));
            }            
        } else {
            final NavbarButton loginLink = new NavbarButton(LoginPage.class, Model.of("Login"));
               loginLink.setIconType(GlyphIconType.login);
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
            AuthenticationHandler.handleAuthentication(getSession());
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
        try {
            if(!getUser().getName().equalsIgnoreCase("anonymous")) {
                return true;
            }
        } catch(WicketRuntimeException ex) {}
        
        return false;
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
    
    public String getContextPath() {
        ServletContext servletContext = WebApplication.get().getServletContext(); 
        String contextPath = servletContext.getContextPath();
        return contextPath;
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
