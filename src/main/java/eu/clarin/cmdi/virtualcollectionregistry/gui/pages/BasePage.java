package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class BasePage extends WebPage {

    protected BasePage(IModel<?> model) {
        super(model);
        // authentication state
        add(new AuthenticationStatePanel("authstate"));

        // main navigation menu
        final Menu menu = new Menu("menu");
        menu.addMenuItem(new MenuItem<BrowsePublicCollectionsPage>(
                new Model<String>("Virtual Collections"),
                BrowsePublicCollectionsPage.class));
        menu.addMenuItem(new MenuItem<BrowsePrivateCollectionsPage>(
                new Model<String>("My Virtual Collections"),
                BrowsePrivateCollectionsPage.class));
        menu.addMenuItem(new MenuItem<CreateVirtualCollectionPage>(
                new Model<String>("Create Virtual Collection"),
                CreateVirtualCollectionPage.class));
        menu.addMenuItem(new MenuItem<AdminPage>(
                new Model<String>("Admin Page"),
                AdminPage.class));
        add(menu);
    }

    protected BasePage() {
        this(null);
    }

    @Override
    protected void onBeforeRender() {
        // skip lazy auto-auth for login page
        if (!this.getClass().isInstance(LoginPage.class)) {
            final HttpServletRequest request =
                getWebRequestCycle().getWebRequest().getHttpServletRequest();
            final ApplicationSession session =
                (ApplicationSession) getSession();
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
        return userName != null && ((Application)getApplication()).isAdmin(userName);
    }
    
    @Override
    public ApplicationSession getSession() {
        return (ApplicationSession) super.getSession();
    }
    
    

} // class BasePage
