package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class BasePage extends WebPage {

    @SpringBean
    private AdminUsersService adminUsersService;

    public static final String BETA_MODE = "eu.clarin.cmdi.virtualcollectionregistry.beta_mode";
    
    protected BasePage(IModel<?> model) {
        super(model);
        
        final boolean beta_mode = Boolean.valueOf(WebApplication.get().getServletContext().getInitParameter(BETA_MODE));
        
        WebMarkupContainer betaBadge = new WebMarkupContainer ("betabadge");
        betaBadge.setVisible(beta_mode);
        add(betaBadge);
        
        // authentication state
        add(new AuthenticationStatePanel("authstate"));

        // main navigation menu
        final Menu menu = new Menu("menu");
        menu.addMenuItem(new MenuItem<>(Model.of("Virtual Collections"),
                BrowsePublicCollectionsPage.class));
        menu.addMenuItem(new MenuItem<>(Model.of("My Virtual Collections"),
                BrowsePrivateCollectionsPage.class));
        menu.addMenuItem(new MenuItem<>(Model.of("Create Virtual Collection"),
                CreateVirtualCollectionPage.class));
        menu.addMenuItem(new MenuItem<>(Model.of("Help"),
                HelpPage.class));        
        menu.addMenuItem(new MenuItem<>(Model.of("Admin Page"),
                AdminPage.class));
        add(menu);

        add(new FeedbackPanel("feedback"));

        add(new BookmarkablePageLink("homelink", getApplication().getHomePage())
                .setAutoEnable(false));
        add(new BookmarkablePageLink("aboutlink", AboutPage.class)
                .setAutoEnable(false));

    }

    protected BasePage() {
        this(null);
    }

    @Override
    protected void onBeforeRender() {
        // skip lazy auto-auth for login page
        if (!this.getClass().isInstance(LoginPage.class)) {
            final HttpServletRequest request
                    = getWebRequestCycle().getWebRequest().getHttpServletRequest();
            final ApplicationSession session
                    = (ApplicationSession) getSession();
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

    protected Principal getUser() {
        ApplicationSession session = (ApplicationSession) getSession();
        Principal principal = session.getPrincipal();
        if (principal == null) {
            throw new WicketRuntimeException("principal == null");
        }
        return principal;
    }

    protected boolean isUserAdmin() {
        final String userName = getUser().getName();
        return userName != null && adminUsersService.isAdmin(userName);
    }

    @Override
    public ApplicationSession getSession() {
        return (ApplicationSession) super.getSession();
    }

} // class BasePage
