package de.mpg.aai.shhaa;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import de.mpg.aai.shhaa.context.AuthenticationContext;
import de.mpg.aai.shhaa.context.AuthenticationContextHolder;

/**
 * HttpServletRequestWrapper to provide proper user information 
 * overriding {@link HttpServletRequest#getRemoteUser()} and {@link HttpServletRequest#getUserPrincipal()};
 * further provides {@link AuthenticationContext}
 * @see HttpServletRequest#getRemoteUser()
 * @see HttpServletRequest#getUserPrincipal()
 * @see #getAuthenticationContext()
 * 
 * @author megger
 */
public class AAIServletRequest extends HttpServletRequestWrapper {
	/** holds possible return targets for rerouting (redirect or forward) the current request to */
	private String					target;
	
	
	/**
	 * initializing constructor
	 * @param request the current, original request to wrap
	 * @param ctx the current request's authentication context
	 */
	public AAIServletRequest(HttpServletRequest request) {
		super(request);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRemoteUser() {
		AuthenticationContext authnCtx = this.getAuthenticationContext();
		return authnCtx != null
			? authnCtx.getAuthPrincipal().getName()
			: super.getRemoteUser();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Principal getUserPrincipal() {
		AuthenticationContext authnCtx = this.getAuthenticationContext();
		return authnCtx != null
			? authnCtx.getAuthPrincipal()
			: super.getUserPrincipal();
	}
	
	
	/**
	 * @return provides this request's authentication context 
	 */
	public AuthenticationContext getAuthenticationContext() {
		return AuthenticationContextHolder.get(this);
	}
	
	/**
	 * @return provides possible return targets to reroute (forward|redirect) this request to
	 */
	public String getTarget() {
		return this.target;
	}
	/**
	 * sets return targets to reroute (forward|redirect) this request to
	 * @param url the return target as string (can be e.g. a servlet call within a webapp context, or a "full" url
	 */
	public void setTarget(String url) {
		this.target = url;
	}
}
