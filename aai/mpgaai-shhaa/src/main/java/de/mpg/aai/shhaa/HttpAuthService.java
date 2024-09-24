package de.mpg.aai.shhaa;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.security.auth.AuthException;
import de.mpg.aai.shhaa.authn.AuthenticationException;
import de.mpg.aai.shhaa.authn.AuthenticationHandler;
import de.mpg.aai.shhaa.authz.AuthorizationException;
import de.mpg.aai.shhaa.authz.AuthorizationHandler;
import de.mpg.aai.shhaa.authz.Location;
import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.config.Configuration;
import de.mpg.aai.shhaa.context.AuthenticationContext;
import de.mpg.aai.shhaa.context.AuthenticationContextHolder;
import de.mpg.aai.shhaa.resolver.AttributeResolverHandler;


/**
 * the aai-authn/z handler, main service 
 * to handle fetching authentication data, authorization 
 * and setting return targets (basic "view control") 
 * @author megger
 *
 */
public class HttpAuthService implements Configurable {
	/** @see java.io.Serializable */
	private static final long serialVersionUID = -7949281926092182526L;
	/** the logger */
	private static Logger	log = LoggerFactory.getLogger(HttpAuthService.class);
	/** holds the configuration */
	private Configuration 				config;
	/** main handler for fetching authentication- & session data */
	private AuthenticationHandler		authnCtxHandler;
	/** main handler for resolving/composition of the current principals attributes */
	private AttributeResolverHandler	attbResolver;
	/** main handler for authorization */
	private AuthorizationHandler		authzHandler;
	
	
	/**
	 * default constructor
	 */
	public HttpAuthService() {
	}
	
	/**
	 * @param conf sets the configuration
	 * @see #config
	 */
	public void setConfig(Configuration conf) {
		this.config = conf;
	}
	/**
	 * @param handler sets the main authentication-handler
	 * @see #authnCtxHandler
	 */
	public void setAuthnCtxHandler(AuthenticationHandler handler) {
		this.authnCtxHandler = handler;
	}
	/**
	 * @param handler the main handler for attribute resolving/composition
	 * @see #attbResolver
	 */
	public void setAttbResolver(AttributeResolverHandler handler) {
		this.attbResolver = handler;
	}
	/**
	 * @param handler the main handler for authorization
	 * @see #authzHandler 
	 */
	public void setAuthzHandler(AuthorizationHandler handler) {
		this.authzHandler = handler;
	}
	
	/**
	 * main function to handle the whole authn/z flow, handles 
	 * retrieval of the current authentication context (status),
	 * loads user attributes (for access-control) 
	 * checks access-permission (authorization) of the current user for the requested resource
	 * and provides {@link AAIServletRequest} with proper authentication data 
	 * (and possibly a reroute target (e.g. fwd to "access-denied" page) 
	 * @param request current request to check 
	 * @param response current request's response
	 * @return AAIServletRequest with proper authentication data (principal)
	 * @throws AuthException if something failed in the control flow (not on e.g. access-denied, such is handled inside) 
	 */
	public AAIServletRequest handleAuth(HttpServletRequest request, HttpServletResponse response) throws AuthException {
		log.trace("starting handle aai authn/z for request {}", request.getRequestURI());
		if(this.isIgnored(request)) {
			log.trace("loction {} is ingored", request.getRequestURI());
			return null;
		}
		AuthenticationContext curAuthCtx = null;
		try {
			// get previous (request) & current authN-context 
			// and compare them to determine changes
			//	( <=> shib login might have changed externally!)
			AuthenticationContext prevAuthCtx = AuthenticationContextHolder.get(request);
			log.trace("tried existing/previous AuthenticationContext - found: {}", prevAuthCtx);
			// load current: loads 
			//	- (shib)session (id, idp, loginTime)
			//	- and subject-with-username (but no further (authorizing) attributes
			//	loads fallback data in case no shib-session/username exists
			curAuthCtx = this.authnCtxHandler.loadAuthenticationContext(request);
			log.trace("loaded current AuthenticationContext - got: {}", curAuthCtx);
			
			
			/**	- USE CASES -
			 * 
			 *	prev. authCtx:	|	null	|	null	|	fb		|	fb		|	fb-x	|	shib	|	shib	|	shib-x
			 * 	cur. authCtx:	|	fb		|	shib	|	fb		|	shib	|	fb-y	|	fb		|	shib	|	shib-y
			 * ----------------------------------------------------------------------------------------------------------------
			 *	use-case:		|	initial	|  initial	|	fb-		| logged-in	|	changed	| logged-out|	shib-	| changed
			 *					|			| logged-in	| session	|			| fb-session| /expired	|	session	| shib-session
			 * ----------------------------------------------------------------------------------------------------------------
			 * action:			|	--		| usrInfo*	|	--		| usrInfo	|	--		| expired	|	--		| expired
			 * 
			 * 	- userInfo: 	display current authCtx data: session, user, attributes
			 *	- userInfo*:	dto, but only if webapp is not in "module" mode 
			 *	- expired:		display "session has expired" + new session's usrInfo
			 */

			String action = request.getParameter(this.config.getActionParam());
			
			// first check forced login/logout, (webapp) external targets 
				
			// FORCED LOGIN - ignore current state and force to SSO 
			if(this.isAction(action, this.config.getSsoAction())) {
				String login = this.genSsoUrl(request);
				log.trace("handling (forced) sso query, to: {}", login);
				return this.prepareResult(curAuthCtx, request, login);
			}
			// FORCED LOGOUT - ignore current state and force to SLO 
			if(this.isAction(action, this.config.getSloAction())) {
				String logout = this.genSloUrl(request);
				log.trace("handling (forced) slo query, to: {}", logout);
				return this.prepareResult(curAuthCtx, request, logout);
			}
			
			// no external targets (sso|slo) -> continue checks
			boolean refresh = this.isAction(action, this.config.getRefeshAction());
			boolean same = this.isSameSession(prevAuthCtx, curAuthCtx);
			
			// check whether to refresh before continue
			if(!same || refresh) {
				log.trace("refreshing attributes...");
				this.attbResolver.loadAttributes(curAuthCtx, request);
			} else if(same) {	// same autCtx -> re-use previously looked up attributes 
				curAuthCtx = prevAuthCtx;
			}
			
			// all data up2date now...
			// => check authorization 
			// 	(even for explicit shhaa pages queries! (see below) <=> calls only allowed from valid locations)
			this.handleAuthz(curAuthCtx, request, response);
			
			// check explicit queries to shhaa internal pages
			// LOGIN-INFO PAGE
			if(this.isAction(action, this.config.getInfoAction())) {
				String info = this.config.getInfoPage();
				log.trace("handling (forced) info-page, to: {}", info);
				return this.prepareResult(curAuthCtx, request, info);
			}
			// EXPIRED PAGE
			if(this.isAction(action, this.config.getExpiredAction())) {
				String expired = this.config.getExipredPage();
				log.trace("handling (forced) expired-page, to: {}", expired);
				return this.prepareResult(curAuthCtx, request, expired);
			}
			// NO-ACCESS PAGE
			if(this.isAction(action, this.config.getDeniedAction())) {
				String denied = this.config.getDeniedPage();
				log.trace("handling (forced) denied-page, to: {}", denied);
				return this.prepareResult(curAuthCtx, request, denied);
			}
			
			// ok now internal targets handled 
			// => ACTUAL SESSION BEHAVIOR: LOGIN- & EXPIRED handling
			// as defined in matrix (-> 'use-cases' comment block above)
			
			// expired ?	<=> prev-shib && changed
			if(prevAuthCtx != null 
			&& !prevAuthCtx.isFallback() && !same) {
				String expired = this.config.getExipredPage();
				log.trace("caught expired session, reroute to {}", expired);
				return this.prepareResult(curAuthCtx, request, expired);
			}
			
			// new (valid shib) session/login-info?  <=> cur-shib && prev-noShib (fb || null) 
			if(!curAuthCtx.isFallback()) {
				String info = this.config.getInfoPage();
				if(prevAuthCtx == null) {
					// first INITIAL ACCESS to protected app: 
					// check module flag:
					// if module: filtered app is just a module integrated in another app
					//	<=> no loginInfo on initial access, cause presumably already shown outside 
					if(this.config.isModule())
						return this.prepareResult(curAuthCtx, request, null);
					// else: initial access to "stand-alone" webapp: show initial login-info
					log.trace("initial access with valid session, , reroute to {}", info);
					return this.prepareResult(curAuthCtx, request, info);
				}
				else if(prevAuthCtx.isFallback()) {
					// NEW LOGIN
					log.trace("login with valid session, , reroute to {}", info);
					return this.prepareResult(curAuthCtx, request, info);
				}
			}
			
			// the "REST": no special notifications (expired/login-info) <=> continue original request
			log.trace("no special use-case, continue original request...");
			return this.prepareResult(curAuthCtx, request, null);
			// something is wrong if not returned yet...
//			throw new IllegalStateException("unhandled aai-auth-filter usecase!");
		} catch(AuthenticationException aE) {
			// something went terribly wrong, can't handle -> throw
			throw aE;
		} catch(AuthorizationException aE) {
			// first check auto-login mode: 
			//	if auto-login && user not logged-in yet -> redirect to sso-login (if sso url has been configured, too)
			if(curAuthCtx != null && curAuthCtx.isFallback()) {
				String login = this.genSsoUrl(request);
				if(this.config.isAutoLogin() && login != null) {
					log.trace("handling (auto) sso query, to: {}", login);
					return this.prepareResult(curAuthCtx, request, login);
				}
			}
			String noaccess = this.config.getDeniedPage();
			log.trace("caught not-authorized: {}, reroute to {}", aE.getMessage(), noaccess);
			return this.prepareResult(curAuthCtx, request, noaccess);
		}
	}
	
	/**
	 * determines whether the given actionValue matches the  given destined action-target
	 * @param value found value
	 * @param target expected target
	 * @return true if value matches target and none is null
	 */
	private boolean isAction(String value, String target) {
		return value != null 
			&& !value.isEmpty()
			&& target != null		// action only "enabled" if (target has been) configured! 
			&& value.equalsIgnoreCase(target);
	}
	
	
	/**
	 * builds the proper Single-Sign-On url 
	 * with appropriate return target: back to the invoking/current request's address
	 * @param request the current/invoking request
	 * @return SSO URL with return target, null if no sso url has been configured
	 */
	private String genSsoUrl(HttpServletRequest request) {
		String param = "target";
		return genReturnTarget(this.config.getSSO(), param, request);
	}
	/**
	 * builds the proper Single-Log-Out url 
	 * with appropriate return target: back to the invoking/current request's address
	 * @param request the current/invoking request
	 * @return SLO URL with return target, null if no slo url has been configured
	 */
	private String genSloUrl(HttpServletRequest request) {
		String param = "return";
		return genReturnTarget(this.config.getSLO(), param, request);
	}
	
	/**
	 * generates the proper return target (parameter) for shibboleth SLO/SSO requests 
	 * @param shibTarget the shibboleth SSO/SLO url 
	 * @param param  the parameter name used for/by shibboleth SSO/SLO
	 * @param request original request, used to extract proper return path (context- & servlet-path) 
	 * @return url to shib SSO/SLO with proper return target to original/invoking request target,
	 * null if no sso-/slo- target-url has been configured
	 */
	private String genReturnTarget(URL shibTarget, String param, HttpServletRequest request) {
		if(shibTarget == null)	// no sso/slo base url configured
			return null; 
		final String host;
		if(this.config.getHost() != null)
			host = this.config.getHost().toExternalForm();
		else {
			StringBuffer hostUrl = new StringBuffer(request.getScheme());
			hostUrl.append("://").append(request.getLocalName());
			host = hostUrl.toString();
		}
                
		final String ctxPath = this.config.getcontextPath() != null
			? this.config.getcontextPath()
			: request.getContextPath();                
                
		final StringBuffer result = new StringBuffer(shibTarget.toExternalForm());
		result.append("?").append(param).append("=");
                
		final StringBuffer returnTarget = new StringBuffer();
		returnTarget.append(host);
                if(host.endsWith("/") && ctxPath.startsWith("/")) {                        
                        returnTarget.append(ctxPath.substring(1));
                }

		// build proper return-to url: incl servlet path & query parameter
		final String spath = request.getServletPath();
		if(spath != null && !spath.isEmpty()) {
			if(ctxPath.endsWith("/"))
				returnTarget.deleteCharAt(result.length()-1);
                        
                        if(returnTarget.toString().endsWith("/") && spath.startsWith("/")) {
                            returnTarget.deleteCharAt(returnTarget.length()-1);
                        }
                        
			returnTarget.append(spath);
		}
                
                // add servlet path path info
                final String pathInfo = request.getPathInfo();
                if(pathInfo != null && !pathInfo.isEmpty()){
                    returnTarget.append(pathInfo);
                }
                
		// don't forget to append original request's query parameter for proper return-to 
		String query = request.getQueryString();
		if(query != null && !query.isEmpty()) {
			// first check-for and remove any filter-control parameter ("action")
			// cut out shhaa action parameter, to avoid redirection loop
			String action = this.config.getActionParam();
			query = this.modParameter(action, query);
			if(query != null && !query.isEmpty())
				returnTarget.append("?").append(query);
		}
                
		try {
			result.append(URLEncoder.encode(returnTarget.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException ueE) {
			throw new IllegalStateException("could not encode url " + returnTarget, ueE);
		}
                
		return result.toString();
	}
	/**
	 * modifies given parameter to provide proper return-target parameter string:
	 * checks the given query for the given parameter and cuts it out (remove) if present,
	 * (url) escapes parameter separator   
	 * @param optional: param the parameter name to look for (cutting out), can be null (no cut out)
	 * @param query the original parameter query string
	 * @return url escaped query string with removed (optionally) given parameter
	 */
	private String modParameter(String param, String query) {
		if(query == null || query.isEmpty())
			return null;
		String[] params = query.split("&");
		StringBuffer result = new StringBuffer();
		for(String item : params) {
			if(param != null && !param.isEmpty())
				if(item.startsWith(param))
					continue;
			if(result.length() > 0)
				result.append("&"); 
			result.append(item);
		}
		return result.toString();
	}
	
	/**
	 * determines whether the current request (location) can be ignored for handling
	 * @param request current request to check
	 * @return true if given request's target(destination) did match the configured ignore-list 
	 */
	private boolean isIgnored(HttpServletRequest request) {
		String path = request.getServletPath();
		log.trace("checking ignores for path {}", path);
		List<Location> ignores = this.config.getIgnores();
		if(ignores == null || ignores.isEmpty())
			return false;
		for(Location ignore : ignores) {
			if(ignore.matchesPath(path))
				return true;
		}
		return false;	// no matches
	}
	
	
	/**
	 * builds the proper return {@link AAIServletRequest} 
	 * for the main function {@link #handleAuth(HttpServletRequest, HttpServletResponse)}:
	 * wraps given {@link AuthenticationContext} into result, 
	 * sets read-only flag when configured,
	 * sets given (fwd/redirect) target;
	 * and puts the resulting authentication context into the current session 
	 * AND into the current {@link AuthenticationContextHolder}
	 * @param authCtx resulting/current authentication context
	 * @param request current handled request
	 * @param target can be null: in case return target to forward/redirect the current request (page-flow control) 
	 * @return proper/up2date return value for {@link #handleAuth(HttpServletRequest, HttpServletResponse)}
	 * @see AAIServletRequest
	 * @see AuthFilter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain) 
	 */
	private AAIServletRequest prepareResult(AuthenticationContext authCtx, HttpServletRequest request, String target) {
		// synchronize attributes: subject to (shhaa)principal
		authCtx.getAuthPrincipal().setAttribues(authCtx.getAuthAttributes());
		if(this.config.isReadOnly() && !authCtx.isReadOnly()) {
			authCtx.setReadOnly();
		}
		AAIServletRequest result = AuthenticationContextHolder.put(request, authCtx);
		if(target != null)
			result.setTarget(target);
		return result;
	}
	
	/**
	 * invokes the authorization checks for the current user ({@link AuthenticationContext}) 
	 * on the current request
	 * @param curAuthCtx the current authentication context (hold current user and his credentials - the attributes)
	 * @param request current request to check access to (its destination)
	 * @param response current request's response
	 */
	private void handleAuthz(AuthenticationContext curAuthCtx, HttpServletRequest request, HttpServletResponse response) {
		this.authzHandler.checkAccess(curAuthCtx, request, response);
	}
	
	/**
	 * determines whether given authentication-contexts represent the same status 
	 * @param ac0
	 * @param ac1
	 * @return true if BOTH args are not null AND both aaiSessionID exists (both not null) and equals 
	 */
	private boolean isSameSession(AuthenticationContext ac0, AuthenticationContext ac1) {
		if(ac0 == null || ac1 == null)
			return false;
		String id0 = ac0.getSessionID();
		String id1 = ac1.getSessionID(); 
		if(id0 == null || id1 == null)
			return false;
		return id0.equals(id1);
	}


}
