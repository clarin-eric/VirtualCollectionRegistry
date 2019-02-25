/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth;

import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class AuthenticationHandler {
    
    private static Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);
    
    public static void handleBasicLogout(final ApplicationSession session, final WebPage page) {
        final RequestCycle cycle =  RequestCycle.get();
        final HttpServletRequest request = 
            (HttpServletRequest)cycle.getRequest().getContainerRequest();

        final Principal principal = request.getUserPrincipal();
        if(!isValidSignedInPrincipal(principal)) {
            logger.info("No logged in principal");
        } else {
            logger.info("Logging out principal = "+principal.toString());
        }

        session.invalidate();
        //TODO: howto handle shibboleth single logout?
         logger.debug("Logout finished, redirecting to homepage");
        //throw new RestartResponseAtInterceptPageException(Application.get().getHomePage());
        
        throw new RedirectToUrlException("https://local.vcr.clarin.eu/Shibboleth.sso/Logout");
    }
    
    public static void handleShibbolethLogout(final ApplicationSession session, final WebPage page) {
        final RequestCycle cycle =  RequestCycle.get();
        final HttpServletRequest request = 
            (HttpServletRequest)cycle.getRequest().getContainerRequest();

        final Principal principal = request.getUserPrincipal();
        if(!isValidSignedInPrincipal(principal)) {
            logger.info("No logged in principal, redirecting to homepage");
            throw new RestartResponseAtInterceptPageException(Application.get().getHomePage());
        } else {
            logger.info("Logging out principal = "+principal.toString()+", redirecting to sp logout endpoint");
            throw new RedirectToUrlException("https://local.vcr.clarin.eu/Shibboleth.sso/Logout");
        }
    }
    
    public static void handleLogin(final ApplicationSession session, final WebPage page) {
        logger.debug("Handling login");
        
        final RequestCycle cycle =  RequestCycle.get();
        final HttpServletRequest request = 
            (HttpServletRequest)cycle.getRequest().getContainerRequest();
        final HttpServletResponse response = 
            (HttpServletResponse)cycle.getResponse().getContainerResponse();

        dumpHeaders(request);
        
        final Principal principal = request.getUserPrincipal();            
        if( isValidSignedInPrincipal(principal)) {
            logger.debug("Principal: "+principal.getName());
            if (session.signIn(principal)) {
                logger.debug("Signed in");
                page.continueToOriginalDestination();
                logger.debug("No original destination, redirecting to homepage");
                // if we reach this line there was no intercept page, so go to home page
                throw new RestartResponseAtInterceptPageException(
                    Application.get().getHomePage());
            } else {
                logger.debug("Access denied");
                throw new RestartResponseException(
                        Application.get().getApplicationSettings()
                            .getAccessDeniedPage());
            }
        }
    }
    
    public static void handleAuthentication(final ApplicationSession session) {              
        logger.debug("Checking authentication");
        final RequestCycle cycle =  RequestCycle.get();
        final HttpServletRequest request = 
            ((ServletWebRequest)cycle.getRequest()).getContainerRequest();

        //dumpHeaders(request);
        
        final Principal principal = request.getUserPrincipal();
        if (!session.isSignedIn()) {
            logger.debug("Not signed in");
            if (request.getAuthType() != null) {
                logger.debug("Auth, but no authed session -> login");
                if (!session.signIn(principal)) {
                    throw new RestartResponseException(Application.get()
                            .getApplicationSettings()
                            .getAccessDeniedPage());
                }
            } else {
                logger.debug("No authtype available, principal = " + request.getUserPrincipal());
            }
        } else {           
            if (!isValidSignedInPrincipal(principal)) {
                logger.warn("Lost Session!");
                session.invalidate();
                throw new RestartResponseException(Application.get()
                        .getApplicationSettings()
                        .getPageExpiredErrorPage());
            } else {
                logger.debug("Already signed in");
            }
        }
    }
    
    protected static void dumpHeaders(HttpServletRequest request) {
        logger.debug("Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        String name = null;
        while((name = headerNames.nextElement()) != null) {
            logger.debug("    "+name+"="+decodeHeaderValue(name, request.getHeader(name)));
        }
    }

    protected static boolean isValidSignedInPrincipal(Principal principal) {
        if( principal == null || principal.getName() == null) {
            logger.debug("No principal (null)");            
            return false;
        } else if (principal.getName().equalsIgnoreCase("anonymous")) {
            logger.debug("Anonymous principal ("+principal.getName()+")");
            return false;
        } else {
            return true;        
        }        
    }
    
    private static String decodeHeaderValue(String name, String value) {
            if(value == null) {
                return null;
            }
            
            try {
                return new String(value.getBytes("ISO8859-1"),"UTF-8");
            } catch(UnsupportedEncodingException ex) {
                logger.error(String.format("Failed to decode header [%s] value [%s] as UTF-8. Error=%s.", name, value, ex.getMessage()));
                logger.debug("Stacktrace:", ex);
            }
            return value;
        }
}
