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
    private Principal principal;
    private boolean isAdmin;
    
    public ApplicationSession(Request request) {
        super(request);
    }

    public boolean signIn(Principal principal) {
        boolean result = false;
        if (principal != null) {
            result = signIn(principal.getName(), null);
            if (result) {
                // XXX: possibly do something with attributes
                this.principal = principal; 
                this.isAdmin =
                    ((Application) getApplication()).isAdmin(getUser());
            }
        }
        return result; 
    }

    @Override
    public boolean authenticate(String username, String password) {
        if (username != null) {
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

    public Principal getPrincipal() {
        return principal;
    }

    public String getUser() {
        return principal.getName();
    }

    public static ApplicationSession get() {
        return (ApplicationSession) AuthenticatedWebSession.get();
    }

} // class ApplicationSession