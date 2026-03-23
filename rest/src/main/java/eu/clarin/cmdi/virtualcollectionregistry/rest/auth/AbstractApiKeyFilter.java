/*
 * Copyright (C) 2026 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.rest.auth;

import eu.clarin.cmdi.virtualcollectionregistry.core.apikey.ApiKeyException;
import eu.clarin.cmdi.virtualcollectionregistry.core.apikey.ApiKeyService;
import eu.clarin.cmdi.virtualcollectionregistry.core.jwt.JwtUtil;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author wilelb
 */
public abstract class AbstractApiKeyFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AbstractApiKeyFilter.class);

    private static final String REALM = "vcr";
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Autowired
    private ApiKeyService apiKeyService;
    
    @Autowired
    private VcrConfig vcrConfig;
    
    //TODO: read this from config
    //private final String secret = "ghekjghejkhgjkehjkfhgcilqu;ioqhq!;oijf;qhvbwl";
    private JwtUtil jwtUtil;
    
    public AbstractApiKeyFilter() {
    //    this.jwtUtil = new JwtUtil(vcrConfig.getJwtSecret());
    }
    /*
    public AbstractApiKeyFilter(ApiKeyService apiKeyService, VcrConfig vcrConfig) {
        this.jwtUtil = new JwtUtil(vcrConfig.getJwtSecret());
    }
    */
    
    @PostConstruct
    public void init() {
        //if(this.jwtUtil == null) {
            this.jwtUtil = new JwtUtil(vcrConfig.getJwtSecret());
        //}
    }
    /**
     * Check the request for availability of an authorization header.
     * 
     * The filter chain is aborted if:
     * - no authorization header is present
     * - or the key in the authorization headers is invalid.
     * 
     * @param requestContext
     * @throws IOException 
     */
    protected void performFilter(ContainerRequestContext requestContext) throws IOException {
        performFilter(requestContext, false);
    }
    
    /**
     * Check the request for availability of an authorization header.
     * 
     * The filter chain is aborted if:
     * - allowAnonymousAccess = false
     * - no authorization header is present
     * - or the key in the authorization headers is invalid.
     * 
     * @param requestContext
     * @param allowAnonymousAccess
     * @throws IOException 
     */
    protected void performFilter(ContainerRequestContext requestContext, boolean allowAnonymousAccess) throws IOException {
        logger.trace("Filtering API key from request");

        //TODO: move back into constructor with autowiring
        //if(this.jwtUtil == null) {
        //    this.jwtUtil = new JwtUtil(vcrConfig.getJwtSecret());
        //}
        
        // Get the Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            logger.debug("Authentication is not token based: "+authorizationHeader);
            abortWithUnauthorized(requestContext, allowAnonymousAccess);
            return;
        }

        
        // Extract the token from the Authorization header
        String username = null;
        String token = authorizationHeader;
        if(!AUTHENTICATION_SCHEME.isEmpty()) {
            //JWT based authentication, typically for interaction from the frontend 
            token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();            
            Claims claims = jwtUtil.parseToken(token);
            username = claims.getSubject();
        } else {
            // Personal API key based authentication, typically used from user cli interaction with this API. 
            //Validate the received token
            try {
                username = validateToken(token);
            } catch (Exception ex) {
                logger.info("Token failed to validate.", ex);
                abortWithUnauthorized(requestContext, allowAnonymousAccess);
            }   
        }
        
        // Update the security context based on the information associated with the token
            try {
                updatePrincipal(requestContext, username);
            } catch(Exception ex) {
                logger.error("Failed to set principal.", ex);
                abortWithUnauthorized(requestContext, allowAnonymousAccess);
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
    protected boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null &&
                (AUTHENTICATION_SCHEME.isEmpty() || (authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ")));
    }

    /**
     * Abort the filter chain with a 401 status code response if anonymous
     * access is not allowed. The WWW-Authenticate header is sent along with 
     * the response.
     *
     * @param requestContext
     * @param allowAnonymousAccess
     */
    protected void abortWithUnauthorized(ContainerRequestContext requestContext, boolean allowAnonymousAccess) {
        if(!allowAnonymousAccess) {
            logger.debug("Api key not found or invalid, aborting access");
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .header(
                        HttpHeaders.WWW_AUTHENTICATE,
                        AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                    .build());
        } else {
            logger.debug("Api key not found or invalid, continueing as anonymous user");
        }
    }

    /**
     * Check if the token was issued by the server and if it's not expired return
     * the username associated with the token.
     *
     * @param token
     * @return 
     * @throws Exception if the token is invalid
     */
    protected String validateToken(String token) throws Exception {
        logger.info("Validating token: ["+token+"]");
        String username = null;
        try {
            username = apiKeyService.getUserForApiKey(token).getName();
        } catch (ApiKeyException ex) {
            throw ex;
        }
        return username;
    }

    /**
     * Update the requests security context with the supplied username.
     *
     * @param requestContext
     * @param username
     * @throws Exception
     */
    protected void updatePrincipal(ContainerRequestContext requestContext, final String username) throws Exception {
        //final String username = "test";
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
