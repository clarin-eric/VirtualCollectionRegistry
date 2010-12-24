package eu.clarin.cmdi.virtualcollectionregistry.gui;

import java.security.Principal;
import java.util.regex.Pattern;

import org.apache.wicket.Request;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;

import eu.clarin.cmdi.virtualcollectionregistry.gui.auth.AuthPrincipal;


@SuppressWarnings("serial")
public class ApplicationSession extends AuthenticatedWebSession {
    private static final String[] ATTRIBUTE_NAMES_NAME =
        { "cn", "commonName", "displayName" };
    private static final Pattern PERSITENT_ID_REGEX =
        Pattern.compile("^[^!]+![^!]+![^!]+$");
    private static final Roles ROLES_USER =
        new Roles(Roles.USER);
    private static final Roles ROLES_ADMIN =
        new Roles(new String[] { Roles.USER, Roles.ADMIN}); 
    private String user;
    private boolean isAdmin;
    private String userDisplay;
    
    public ApplicationSession(Request request) {
        super(request);
    }

    public boolean signIn(Principal principal) {
        boolean result = false;
        if (principal != null) {
            result = signIn(principal.getName(), null);
            if (result) {
                user = principal.getName();
                isAdmin = ((Application) getApplication()).isAdmin(user);
                userDisplay = findDisplayName(principal);
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
        return new Principal() {
            @Override
            public String getName() {
                return user;
            }
        };
    }

    public String getUser() {
        return user;
    }

    public String getUserDisplay() {
        if (userDisplay != null) {
            return userDisplay;
        }
        if (PERSITENT_ID_REGEX.matcher(user).matches()) {
            return "Authenticated via Shibboleth";
        }
        return user;
    }

    public static ApplicationSession get() {
        return (ApplicationSession) AuthenticatedWebSession.get();
    }

    private static String findDisplayName(Principal p) {
        if (p instanceof AuthPrincipal) {
            final AuthPrincipal principal = (AuthPrincipal) p;
            for (String attr : ATTRIBUTE_NAMES_NAME ) {
                String name = principal.getAttibute(attr);
                if (name != null) {
                    return name;
                }
            }
            String givenName = principal.getAttibute("givenName");
            String surname = principal.getAttibute("surname");
            if ((givenName != null) && (surname != null)) {
                return givenName + " " + surname;
            }
        }
        return null;
    }

} // class ApplicationSession
