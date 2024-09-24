package de.mpg.aai.shhaa.config;

import de.mpg.aai.shhaa.HttpAuthService;
import de.mpg.aai.shhaa.authn.AuthenticationHandler;
import de.mpg.aai.shhaa.authn.FallbackAuthnMod;
import de.mpg.aai.shhaa.authn.ShibHeaderAuthnMod;
import de.mpg.aai.shhaa.authz.AuthorizationHandler;
import de.mpg.aai.shhaa.resolver.AttributeResolverHandler;
import de.mpg.aai.shhaa.resolver.ShibHeaderAttbResolver;


/**
 * service factory: 
 * loads the services as specified in the configuration
 * @author megger
 */
public class ServiceLoader {
	
	/**
	 * default constructor
	 */
	public ServiceLoader() {
	}
	
	
	/**
	 * builds/loads the services according to the configuration 
	 * @param configCtx context providing the current config
	 * @return HttpAuthService instance as configured - the main authn/z handler
	 */
	static HttpAuthService load(ConfigContext configCtx) {
		// TODO make this configurable 
		Configuration config = configCtx.getConfiguration();
		final HttpAuthService result = new HttpAuthService();
		result.setAuthnCtxHandler(loadAuthenticationCtxHandler(config));
		result.setAttbResolver(loadAttributeResolverHandler(config));
		result.setAuthzHandler(loadAuthorizationHandler(config));
		result.setConfig(config);
		return result;
	}
	
	/**
	 * builds/loads the authentication handler according to the configuration 
	 * @param conf the current configuration
	 * @return AuthenticationHandler instance as configured
	 */
	private static AuthenticationHandler loadAuthenticationCtxHandler(Configuration conf) {
		AuthenticationHandler result = new AuthenticationHandler();
		result.addModule(new ShibHeaderAuthnMod());
		result.addModule(new FallbackAuthnMod());
		result.setConfig(conf);
		return result;
	}
	
	/**
	 * builds/loads the attribute composition handler (resolver) according to the configuration 
	 * @param conf the current configuration
	 * @return AttributeResolverHandler instance as configured
	 */
	private static AttributeResolverHandler loadAttributeResolverHandler(Configuration conf) {
		AttributeResolverHandler result = new AttributeResolverHandler();
		result.addModule(new ShibHeaderAttbResolver());
		result.setConfig(conf);
		return result;
	}
	
	/**
	 * builds/loads the authorization handler according to the configuration 
	 * @param conf the current configuration
	 * @return AuthorizationHandler instance as configured
	 */
	private static AuthorizationHandler loadAuthorizationHandler(Configuration conf) {
		AuthorizationHandler result = new AuthorizationHandler();
		result.setConfig(conf);
		return result;
	}
}
