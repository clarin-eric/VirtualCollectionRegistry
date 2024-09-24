package de.mpg.aai.shhaa.resolver;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.config.Configuration;
import de.mpg.aai.shhaa.context.AuthenticationContext;


/**
 * main attribute composition/resolving handler, 
 * invokes its registered ({@link #resolverModules} resolver modules 
 * and processes them one after another 
 * @see AttributeResolverModule
 * @author megger
 */
public class AttributeResolverHandler implements Configurable {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(AttributeResolverHandler.class);
	/** holds registered resolver modules, to be processed one after another	 */
	private List<AttributeResolverModule>	resolverModules;
	
	
	/**
	 * determines the attributes for the given AuthenticationContext
	 * from the given request (and possibly further sources)
	 * and add them to the given context's Subject's public credentials 
	 * @param authCtx current authentication context
	 * @param request current request 
	 * @throws ResolveException if something failed
	 */
	public void loadAttributes(AuthenticationContext authCtx, HttpServletRequest request)
	throws ResolveException {
		Subject userSubj = authCtx.getSubject();
		for(AttributeResolverModule module : this.resolverModules) {
			// FAILFAST, do not catch Exceptions: we expect every module to succeed
//			try {
				module.resolve(userSubj, request);
//			} catch(ResolveException rE) {
//				log.trace("attributeResolver module {} failed: {}, try next...", module.getClass().getName(), rE.getMessage());
//				continue;	// if any error occurs: skip and continue with next
//			}
		}
	}
	
	
	/**
	 * registers a new AttributeResolverModule and adds it to its internal list of modules to process
	 * @param module AttributeResolverModule to add ("register")
	 * @return true (as specified in {@link List#add(Object)}
	 */
	public boolean addModule(AttributeResolverModule module) {
		if(this.resolverModules == null)
			this.resolverModules = Collections.synchronizedList(new Vector<AttributeResolverModule>());
		log.debug("registering AttributeResolverModule {}", module.getClass().getName());
		return this.resolverModules.add(module);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void setConfig(Configuration config) {
		for(AttributeResolverModule module : this.resolverModules) {
			if(module instanceof Configurable)
				((Configurable) module).setConfig(config);
		}
	}
}
