package eu.clarin.cmdi.virtualcollectionregistry.rest.auth;


import eu.clarin.cmdi.virtualcollectionregistry.core.apikey.ApiKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * References:
 * - https://stackoverflow.com/questions/26777083/best-practice-for-rest-token-based-authentication-with-jax-rs-and-jersey/26778123#26778123
 * - https://stackoverflow.com/questions/27137593/authenticating-with-an-api-key-in-jax-rs
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyAuthFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private static final String REALM = "vcr";
    private static final String AUTHENTICATION_SCHEME = "";

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.info("Filtering API key from request");

        // Get the Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            logger.warn("Authentication is not token based: "+authorizationHeader);
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader;
        if(!AUTHENTICATION_SCHEME.isEmpty()) {
            token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
        }

        // Validate the received token
        try {
            validateToken(token);
        } catch (Exception ex) {
            logger.info("Token failed to validate.", ex);
            abortWithUnauthorized(requestContext);
        }

        // Update the security context based on the information associated with the token
        try {
            updatePrincipal(requestContext, token);
        } catch(Exception ex) {
            logger.error("Failed to set principal.", ex);
            abortWithUnauthorized(requestContext);
        }
    }

    /**
     * Check if the Authorization header is valid
     * It must not be null and must be prefixed with "Bearer" plus a whitespace
     * The authentication scheme comparison must be case-insensitive
     *
     * @param authorizationHeader
     * @return
     */
    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null &&
                (AUTHENTICATION_SCHEME.isEmpty() || (authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ")));
    }

    /**
     * Abort the filter chain with a 401 status code response
     * The WWW-Authenticate header is sent along with the response
     *
     * @param requestContext
     */
    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .build());
    }

    /**
     * Check if the token was issued by the server and if it's not expired
     * Throw an Exception if the token is invalid
     *
     * @param token
     * @throws Exception
     */
    private void validateToken(String token) throws Exception {
        logger.info("Validating token: ["+token+"]");
        try {
            apiKeyService.getUserForApiKey(token);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Fetch the information associated to the token and update the requests security context accordingly.
     *
     * @param requestContext
     * @param token
     * @throws Exception
     */
    private void updatePrincipal(ContainerRequestContext requestContext, String token) throws Exception {
        final String username = "test";
        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> username;
            }
            @Override
            public boolean isUserInRole(String role) {
                return true;
            }
            @Override
            public boolean isSecure() {
                return currentSecurityContext.isSecure();
            }
            @Override
            public String getAuthenticationScheme() {
                return AUTHENTICATION_SCHEME;
            }
        });
    }
}
