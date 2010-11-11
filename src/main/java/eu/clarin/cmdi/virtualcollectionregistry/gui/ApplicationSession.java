package eu.clarin.cmdi.virtualcollectionregistry.gui;

import java.security.Principal;

import org.apache.wicket.Request;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;


@SuppressWarnings("serial")
public class ApplicationSession extends AuthenticatedWebSession {
    private static final Roles ROLES_USER =
        new Roles(Roles.USER);
    private static final Roles ROLES_ADMIN =
        new Roles(new String[] { Roles.USER, Roles.ADMIN}); 
    private String user = null;
    private boolean isAdmin;
    
    public ApplicationSession(Request request) {
        super(request);
    }

    public boolean signIn(Principal principal) {
        boolean result = false;
        if (principal != null) {
            result = signIn(principal.getName(), null);
            // XXX: possibly do something with attributes
        }
        return result; 
    }

    @Override
    public boolean authenticate(String username, String password) {
        if (username != null) {
            user = username;
            isAdmin = ((Application) getApplication()).isAdmin(username);
            replaceSession();
            return true;
        }
        return false;
    }

    @Override
    public Roles getRoles() {
        if (isSignedIn()) {
            return isAdmin ? ROLES_ADMIN : ROLES_USER;
        }
        return null;
    }

    public String getUser() {
        return user;
    }

    public static ApplicationSession get() {
        return (ApplicationSession) AuthenticatedWebSession.get();
    }

} // class ApplicationSession
