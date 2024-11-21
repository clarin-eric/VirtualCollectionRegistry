package de.mpg.aai.shhaa.authz;

import java.util.List;
import java.util.Vector;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.config.Configuration;
import de.mpg.aai.shhaa.context.AuthenticationContext;
import de.mpg.aai.shhaa.model.AuthAttributes;


/**
 * handles authorization checks for current user to current request (destined location)
 * depending on user's attributes; 
 * please look into the configuration file, section 'authorization' for details 
 * @author megger
 */
public class AuthorizationHandler implements Configurable {
	/** the logger */
	private static Logger	log = LoggerFactory.getLogger(AuthorizationHandler.class);
	/** holds the configuration
	 * which holds the configured authorization rules */
	private Configuration	config;
	
	/**
	 * default constructor
	 */
	public AuthorizationHandler() {
	}
	
	
	/**
	 * checks whether given user (represented by authentication context) 
	 * have access to the requested resource 
	 * due to his attributes and the configured authorization rules
	 * @param authCtx current authn-context, representing/holding the current user (and session...)
	 * @param request current request to check (access to its destination)
	 * @param response current request's response
	 * @throws AuthorizationException if user has no access
	 */
	public void checkAccess(AuthenticationContext authCtx, HttpServletRequest request, 
	@SuppressWarnings("unused") HttpServletResponse response)
	throws AuthorizationException {
		this.checkPathAuthz(authCtx, request);
	}
	
	/**
	 * checks whether given user (represented by authentication context) 
	 * have access to the request path (the request's destination) 
	 * @param authCtx current authn-context, representing/holding the current user (and session...)
	 * @param request current request to check (access to its destination)
	 * @throws AuthorizationException if user has no access
	 */
	private void checkPathAuthz(AuthenticationContext authCtx, HttpServletRequest request)
	throws AuthorizationException {
		// this method checks the SERVLET PATH 
		String path = request.getServletPath();
                final String pathInfo = request.getPathInfo();
                if (pathInfo!=null) {
                    path += pathInfo;
                }
		log.trace("checking authZ for path {}", path);
		List<Location> locations = this.getLocationRules(path, request.getMethod());
		// normally there should be only one location(rule-set) per path
		if(locations.size() > 1)
			log.info("found multiple location rules ({}) for single path {}", locations.size(), path);
		log.trace("found matching rules {}", locations.toString());
		this.checkRequirements(authCtx, locations);
	}
	
	
	/**
	 * provides the list of authorization rules which apply (match) for the given path
	 * @param path the target destination to match 
	 * @return list of matched Locations, never null (empty on no-match)
	 * @see Location#matchesPath(String)
	 */
	private List<Location> getLocationRules(String path, String method) {
		List<Location> result = new Vector<Location>();
		List<Location> rules = this.getAuthzRules();
		if(rules == null || rules.isEmpty())
			return result;
		for(Location location : rules) {
			if(location.matchesPath(path) && location.matchesMethod(method))
				result.add(location);
		}
		return result;
	}
	
	
	/**
	 * provides the (configured) authorization rules
	 * @return List of Locations representing the access-rules
	 */
	private List<Location> getAuthzRules() {
		return this.config.getLocationRules();
	}
	
	
	/**
	 * handles actual access-check:
	 * tests whether the given user (represented by AuthenticationContext) 
	 * has access (due to his credentials) to the given list of location/authorization-rules
	 * @param authCtx current authn-context, representing/holding the current user (and session...)
	 * @param locations list of authorization/location rules to check
	 * @throws AuthorizationException
	 */
	private void checkRequirements(AuthenticationContext authCtx, List<Location> locations) 
	throws AuthorizationException {
		if(locations == null || locations.isEmpty())
			return;
		// there ARE protected locations => access exclusively for VALID USER
		//	( => no further attributes checks for anonymous fallback user, has no access anyway)
		if(authCtx.isFallback())
			throw new AuthorizationException("requires valid-user for " + locations.toString());
		// evaluate location rules
		//	if multiple locations: treat as AND <=> must validate all <=> fail fast (on first)
		AuthAttributes credentials = authCtx.getAuthAttributes();
		for(Location location : locations) {
			if(!location.evaluate(credentials))
				throw new AuthorizationException("requirements not met for " + location.toString());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConfig(Configuration conf) {
		this.config = conf;
	}
}
