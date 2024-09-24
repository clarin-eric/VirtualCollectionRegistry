package de.mpg.aai.shhaa.resolver;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;

import de.mpg.aai.shhaa.model.AuthAttribute;


/**
 * interface for modules of {@link AttributeResolverHandler}, 
 * defines methods to resolve (load/retrieve/fetch from where ever) attributes    
 * @author megger
 */
public interface AttributeResolverModule {
	
	/**
	 * resolves (loads/retrieves/fetches) attributes ({@link AuthAttribute}) for the given user
	 * from the given request (or somewhere else)
	 * AND add those as (public) credentials to the given subject 
	 * @param user the subject to look-up the attributes for and add them to
	 * @param request possible datasource to fetch attributes from (http header)
	 * @throws ResolveException if something fails
	 */
	public void resolve(Subject user, HttpServletRequest request) throws ResolveException;

}
