package eu.clarin.cmdi.virtualcollectionregistry.gui;

import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.AuthPrincipal;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import java.security.Principal;
import java.util.regex.Pattern;
import org.apache.wicket.Request;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ApplicationSession extends AuthenticatedWebSession {

    private final static Logger logger = LoggerFactory.getLogger(ApplicationSession.class);

    private static final String[] ATTRIBUTE_NAMES_NAME
            = {"cn", "commonName", "displayName"};
    private static final Pattern PERSITENT_ID_REGEX
            = Pattern.compile("^[^!]+![^!]+![^!]+$");
    private static final Roles ROLES_USER
            = new Roles(Roles.USER);
    private static final Roles ROLES_ADMIN
            = new Roles(new String[]{Roles.USER, Roles.ADMIN});
    private String user;
    private boolean isAdmin;
    private String userDisplay;

    public ApplicationSession(Request request) {
        super(request);
    }

    public boolean signIn(Principal principal) {
        logger.trace("Signing in principal {}", principal);
        boolean result = false;
        if (principal != null) {
            result = signIn(principal.getName(), null);
            if (result) {
                user = principal.getName();
                isAdmin = ((Application) getApplication()).isAdmin(user);
                userDisplay = findDisplayName(principal);
                logger.debug("Principal is signed in [user = {}, display name = {}, isAdmin = {}]", user, userDisplay, isAdmin);
            }
        }
        return result;
    }

    @Override
    public boolean authenticate(String username, String password) {
        return username != null;
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

    /**
     *
     * @param user user to check for
     * @return whether the specified user is the user currently signed in (false
     * if {@link #isSignedIn() } returns false)
     */
    public boolean isCurrentUser(User user) {
        return isSignedIn() && getUser().equals(user.getName());
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
        logger.trace("Looking for display name for principal {}", p);
        if (p instanceof AuthPrincipal) {
            final AuthPrincipal principal = (AuthPrincipal) p;
            for (String attr : ATTRIBUTE_NAMES_NAME) {
                final String name = getAttribute(principal, attr);
                if (name != null) {
                    logger.debug("Display name found for principal: {}", name);
                    return name;
                }
            }
            String givenName = getAttribute(principal, "givenName");
            String surname = getAttribute(principal, "surname");
            if ((givenName != null) && (surname != null)) {
                final String name = givenName + " " + surname;
                logger.debug("Display name found for principal: {}", name);
                return name;
            }
        }
        logger.debug("No display name found for principal");
        return null;
    }

    private static String getAttribute(final AuthPrincipal principal, String attr) {
        logger.trace("Looking for attribute {}", attr);
        final AuthAttribute<?> attribute = principal.getAttribues().get(attr);
        if (attribute != null) {
            final Object value = attribute.getValue();
            if (value != null) {
                logger.trace("Found attribute value: {} = {}", attr, value);
                return value.toString();
            }
        }
        return null;
    }

} // class ApplicationSession
