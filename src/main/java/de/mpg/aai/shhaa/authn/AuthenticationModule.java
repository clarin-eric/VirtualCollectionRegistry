package de.mpg.aai.shhaa.authn;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;

import de.mpg.aai.shhaa.context.AuthenticationContext;

/**
 * interface for modules of {@link AuthenticationHandler}, 
 * defines methods to initialize an authentication context and the (user) subject  
 * @author megger
 *
 */
public interface AuthenticationModule {
	
	/**
	 * initialize the given authentication context with the proper data
	 * NOTE: this method should internally already call {@link #initSubject(AuthenticationContext, HttpServletRequest)} 
	 * @param authCtx AuthenticationContext to be filled with proper data
	 * @param request current request
	 * @throws AuthenticationException if lookup of proper data fails
	 */
	public void initAuthenticationContext(final AuthenticationContext authCtx, HttpServletRequest request)
		throws AuthenticationException;
	
	/**
	 * updates or initializes (if null yet) the given authentication context's Subject with the proper user  
	 * @param authCtx AuthenticationContext to update/init its Subject
	 * @param request current request
	 * @return the new/updated Subject
	 * @throws AuthenticationException if lookup or init of user/subject data failed 
	 */
	public Subject initSubject(final AuthenticationContext authCtx, HttpServletRequest request)
	throws AuthenticationException;

}
