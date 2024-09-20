package de.mpg.aai.shhaa.authn;

import java.util.Date;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.context.AuthenticationContext;


/**
 * implementation of {@link AuthenticationModule} 
 * providing "fallback" AuthenticationContext, Subject/Principal;
 * fallback means the "anonymous"/default data when no other specific login (e.g. by shibboleth/saml) could be found
 * @author megger
 *
 */
public class FallbackAuthnMod extends BaseAuthnMod implements AuthenticationModule, Configurable {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(FallbackAuthnMod.class);
	
	/**
	 * default constructor
	 */
	public FallbackAuthnMod() {
	}
	
	/**
	 * initializes authentication context with fallback data
	 * {@inheritDoc}
	 */
	@Override
	public void initAuthenticationContext(final AuthenticationContext authCtx, HttpServletRequest request) 
	throws AuthenticationException {
		// new fallback context: 
		// init with some sessionID: since no shib -> use jsessionID
		authCtx.setFallback(true);
		HttpSession session = request.getSession();
		if(session == null)
			throw new AuthenticationException("could not find a session");
		String jSessionID = session.getId();
		if(jSessionID == null)
			throw new AuthenticationException("could not find any (j)Session-ID");
		authCtx.setSessionID(jSessionID);
		authCtx.setIdentiyProviderID("webapp-container");
		authCtx.setLoginTime(new Date(session.getCreationTime()));	// now
		log.trace("loaded AuthenticationContext: {}", authCtx.toString());
		this.initSubject(authCtx, request);
	}
	
	
	/**
	 * provides principal/subject with fallback data
	 * {@inheritDoc}
	 */
	@Override
	public Subject initSubject(final AuthenticationContext authCtx, @SuppressWarnings("unused") HttpServletRequest request) 
	throws AuthenticationException {
		log.trace("initializing subject for {}", authCtx);
		// add (new, configured) fallback user
		// inits new subject if does not exist yet:
		String fbUid = this.getConfig().getFallbackUid();
		if(fbUid == null || fbUid.trim().isEmpty())
			throw new AuthenticationException("no fallback uid configured");
		log.trace("found fallback-user {}", fbUid);
		this.addUser(authCtx, fbUid);
		return authCtx.getSubject();
	}
}
