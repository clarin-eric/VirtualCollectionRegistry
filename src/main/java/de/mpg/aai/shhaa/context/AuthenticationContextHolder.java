package de.mpg.aai.shhaa.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import de.mpg.aai.shhaa.AAIServletRequest;

/**
 * ThreadLocal holder for the current authentication context
 * use {@link #get()} to retrieve the context from the application (business) layer 
 * if there is no access to the request/session
 * 
 * @author megger
 */
public class AuthenticationContextHolder {
	public AuthenticationContextHolder() {
	}
	private static final ThreadLocal<AuthenticationContext> authCtxTL = new ThreadLocal<AuthenticationContext>();

	/**
	 * @return the current authentication context
	 */
	public static AuthenticationContext get() {
		return authCtxTL.get();
	}
	/**
	 * @param ctx sets the current authentication context
	 */
	private static void set(AuthenticationContext ctx) {
		AuthenticationContextHolder.authCtxTL.set(ctx);
	}
	
	
	/**
	 * provides the AuthenticationContext instance attribute from the (session within the) given request
	 * @param request request to lookup the AuthenticationContext in
	 * @return AuthenticationContext found in the given request's session, null if not found
	 * @see #get(HttpSession)
	 * @see #put(HttpSession, AuthenticationContext)
	 */
	public static AuthenticationContext get(HttpServletRequest request) {
		HttpSession session = request.getSession();
		return session != null 
			? AuthenticationContextHolder.get(session) 
			: null;
	}
	
	/**
	 * provides the AuthenticationContext instance attribute from the given session
	 * @param session session to lookup the AuthenticationContext in
	 * @return AuthenticationContext found in the given session, null if not found
	 * @see #get(HttpSession)
	 * @see #put(HttpSession, AuthenticationContext)
	 */
	public static AuthenticationContext get(HttpSession session) {
		return (AuthenticationContext) session.getAttribute(AuthenticationContext.class.getName());
	}
	
	/**
	 * stores the given AuthenticationContext instance (as attribute) into the given request's session 
	 * @param request to put the info as attribute into (its associated session) 
	 * @param data the info to put into the session
	 * @see #get(HttpSession)
	 * @see #get(HttpServletRequest) 
	 */
	public static AAIServletRequest put(HttpServletRequest request, AuthenticationContext data) {
		if(data == null)
			throw new IllegalArgumentException("authenticationContext must not be null");
		if(data.getSubject() == null)
			throw new IllegalStateException("authenticationContext has no subject");
		if(data.getAuthPrincipal() == null)
			throw new IllegalStateException("authenticationContext has no AAI-Principal");

		AuthenticationContextHolder.set(data);
		HttpSession session = request.getSession();
		if(session == null)
			throw new IllegalStateException("no session found");
		session.setAttribute(AuthenticationContext.class.getName(), data);
		
		return new AAIServletRequest(request);
	}
}
