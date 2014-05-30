package eu.clarin.cmdi.virtualcollectionregistry.gui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.session.pagemap.LeastRecentlyAccessedEvictionStrategy;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.AdminPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowsePrivateCollectionsPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.CreateVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowsePublicCollectionsPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.LoginPage;

public class Application extends AuthenticatedWebApplication {
    private static final String CONFIG_PARAM_ADMINDB = "admindb";
    private Set<String> adminUsers =
        new HashSet<String>();

    @Override
    protected void init() {
        super.init();

        String s = getServletContext().getInitParameter(CONFIG_PARAM_ADMINDB);
        if (s != null) {
            try {
                loadAdminDatabase(s);
            } catch (IOException e ) {
                // FIXME: handle error
            }
        }
        if (adminUsers.isEmpty()) {
            // FIXME: better logging
            System.err.println("WARNING: no admin users have been defined");
        }
        getMarkupSettings().setDefaultMarkupEncoding("utf-8");
        getRequestCycleSettings().setResponseRequestEncoding("utf-8");
        getSessionSettings().setMaxPageMaps(3);
        getSessionSettings().setPageMapEvictionStrategy(
                new LeastRecentlyAccessedEvictionStrategy(3));
        if (getConfigurationType() != DEPLOYMENT) {
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
        return adminUsers.contains(user);
    }

    private void loadAdminDatabase(String filename) throws IOException {
        adminUsers.clear();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            adminUsers.add(line);
        } // while
        reader.close();
    }

} // class Application
