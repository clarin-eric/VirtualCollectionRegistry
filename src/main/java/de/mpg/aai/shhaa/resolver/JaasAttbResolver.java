package de.mpg.aai.shhaa.resolver;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.security.auth.callback.NameCallbackHandler;
import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.config.Configuration;
import de.mpg.aai.shhaa.model.AuthAttribute;


/**
 * implementation of {@link AttributeResolverModule}
 * to plug-in JAAS for attribute-resolving;
 * takes the jaas config file SECTION name from the configuration and invokes jaas:
 * enables client developers to add thus their own implementations to resolve attributes 
 * for the given Subject (and add them as {@link AuthAttribute} to the Subject's public credentials
 * @see http://java.sun.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html
 * @author megger
 *
 */
public class JaasAttbResolver implements AttributeResolverModule, Configurable {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(JaasAttbResolver.class);
	/** holds the configuration */
	private Configuration config;

	
	/**
	 * default constructor
	 */
	public JaasAttbResolver() {
	}
	
	
	/**
	 * <div>{@inheritDoc}</div>
	 * invokes {@link javax.security.auth.login.LoginContext} 
	 * with a single {@link NameCallbackHandler} providing the given subject's username
	 * (to be used by the jaas LoginModules to retrieve the username in order to lookup attributes for/by it)
	 * @throws ResolveException if something failed
	 */
	@Override
	public void resolve(Subject userSubj, @SuppressWarnings("unused") HttpServletRequest request) 
	throws ResolveException {
		String confName = this.config.getJaasConfigName();
		log.trace("resloving attributes by jaas({})...", confName);
		String uid = userSubj.getPrincipals().iterator().next().getName();
		
		try {
			CallbackHandler cbH = new NameCallbackHandler(uid);
			LoginContext loginCtx = new LoginContext(confName, userSubj, cbH);
			loginCtx.login();
		} catch(LoginException lE) {
			throw new ResolveException("could not resolve all attributes: " + lE.getMessage(), lE);
		}
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void setConfig(Configuration conf) {
		this.config = conf;
	}
}
