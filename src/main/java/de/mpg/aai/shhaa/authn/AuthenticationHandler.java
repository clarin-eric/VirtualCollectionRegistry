package de.mpg.aai.shhaa.authn;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.security.auth.login.LoginException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.config.Configuration;
import de.mpg.aai.shhaa.context.AuthenticationContext;

/**
 * main authentication handler, 
 * invokes its registered ({@link #authModules} authentication modules 
 * and processes them one after another 
 * @see AuthenticationModule
 * @author megger
 *
 */
public class AuthenticationHandler implements Configurable {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(AuthenticationHandler.class);
	/** holds registered authentication modules to be processed one after another */
	private List<AuthenticationModule> authModules;
	
	
	/**
	 * default constructor
	 */
	public AuthenticationHandler() {
	}
		
		
	/**
	 * determines the current AuthenticationContext data from the given request 
	 * @param request source to determine the target data from
	 * @return AuthenticationContext holding data according to given request
	 * @throws LoginException 
	 */
	public AuthenticationContext loadAuthenticationContext(HttpServletRequest request) throws AuthenticationException {
		AuthenticationContext result = newContext();
		List<AuthenticationException> aEs = new Vector<AuthenticationException>();
		for(AuthenticationModule module : this.authModules) {
			try {
				module.initAuthenticationContext(result, request);
				// take the first valid result, on errors exceptions are expected
				return result;
			} catch(AuthenticationException aE) {
				log.trace("authentication module ({}) failed: {}, try next...", module.getClass().getName(), aE.getMessage());
				aEs.add(aE);	// keep track
				continue;	// if any error occurs: skip and try next
			}
		}
		// we expect an actual result: if not returned yet all modules failed
		log.error("all authentication(context-loading) modules failed: {}", aEs.toString());
		throw new AuthenticationException("could not create any authentication context (all modules failed)");
	}
	
	
	/**
	 * loads/provides the fallback authentication context 
	 * @param request current request
	 * @return an authentication context holding/representing the fallback status  
	 * @throws AuthenticationException if something failed
	 */
	public AuthenticationContext loadFallbackContext(HttpServletRequest request) throws AuthenticationException {
		AuthenticationContext result = newContext();
		AuthenticationModule fbMod = this.authModules.get(this.authModules.size()-1);
		if(!(fbMod instanceof FallbackAuthnMod))
			throw new IllegalStateException("could not find FallbackAuthnModule (expected at last index)");
		fbMod.initAuthenticationContext(result, request);
		return result;
	}
	
	
	/**
	 * little factory wrapper, builds and returns a new AuthenticationContext
	 * @return new AuthenticationContext
	 */
	private AuthenticationContext newContext() {
		log.debug("loading new authentication context");
		return new AuthenticationContext();
	}
	
	
	/**
	 * registers a new AuthenticationModule and adds it to its internal list of modules to process
	 * @param module AuthenticationModule to add ("register")
	 * @return true (as specified in {@link List#add(Object)}
	 */
	public boolean addModule(AuthenticationModule module) {
		if(this.authModules == null)
			this.authModules = Collections.synchronizedList(new Vector<AuthenticationModule>());
		log.debug("registering AuthenticationModule {}", module.getClass().getName());
		return this.authModules.add(module);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConfig(Configuration config) {
		for(AuthenticationModule module : this.authModules) {
			if(module instanceof Configurable)
				((Configurable) module).setConfig(config);
		}
	}
}