package de.mpg.aai.shhaa.authn;

import java.security.Principal;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.config.Configuration;
import de.mpg.aai.shhaa.context.AuthenticationContext;
import de.mpg.aai.shhaa.model.AuthPrincipal;


/**
 * abstract base class for implementations of {@link AuthenticationModule}
 * @author megger
 *
 */
public abstract class BaseAuthnMod /*implements Configurable*/ {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(BaseAuthnMod.class);
	/** holds the configuration */
	private Configuration config;
	
	
	/**
	 * @return the configuration
	 */
	public Configuration getConfig() {
		return this.config;
	}
	/**
	 * @see Configurable#setConfig(Configuration)
	 */
	public void setConfig(Configuration conf) {
		this.config = conf;
	}
	
	/**
	 * adds the given user(string), wrapped into a java.security.Principal implementation, 
	 * to the given authentication context's subject
	 * @param authCtx to add the user to
	 * @param uid username to be added
	 * @return added user wrapped in a java.security.Principal impl
	 * @see #toUser(String)
	 * @see AuthPrincipal
	 */
	protected Principal addUser(AuthenticationContext authCtx, String uid) {
		Subject target = authCtx.getSubject();
		if(target == null) {
			log.trace("creating new subject for {}", uid);
			target = new Subject();
			authCtx.setSubject(target);
		}
		Principal user = this.toUser(uid);
		log.trace("adding new principal {}", uid);
		target.getPrincipals().add(user);
		return user;
	}
	
	/**
	 * wrapps the given username into a java.security.Principal
	 * @param uid the username
	 * @return user(name) wrapped in a java.security.Principal impl
	 * @see AuthPrincipal
	 */
	protected Principal toUser(String uid) {
		return new AuthPrincipal(uid);
	}

}
