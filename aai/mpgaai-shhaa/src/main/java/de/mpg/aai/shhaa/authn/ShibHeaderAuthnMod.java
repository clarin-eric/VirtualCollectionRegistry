package de.mpg.aai.shhaa.authn;

import de.mpg.aai.shhaa.HttpHeaderUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.context.AuthenticationContext;


/**
 * implementation of {@link AuthenticationModule}
 * providing data looked up from shibboleth-sp http header attributes  
 * @author megger
 *
 */
public class ShibHeaderAuthnMod extends BaseAuthnMod implements AuthenticationModule, Configurable {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(ShibHeaderAuthnMod.class);
	
	
	/**
	 * provides authentication context with data looked up from given requests shibboleth-SP http header attributes 
	 * {@inheritDoc}
	 */
	@Override
	public void initAuthenticationContext(final AuthenticationContext authCtx, HttpServletRequest request)
	throws AuthenticationException {
		log.trace("loading AuthenticationContext...");
		// we use the shib-session-ID to determine whether there is a (valid) shib session
		String shibSession =this.getHeader(this.getConfig().getShibSessionID(), request);
		// => no shib-session => not logged-in => provide fallback auth-context (incl fallback user) 
		if(shibSession == null || shibSession.trim().isEmpty()) {
			throw new AuthenticationException("no shib-session found");
		}
		log.trace("continue with shib-session {}", shibSession);
		authCtx.setSessionID(shibSession);
		// got valid shib-session, continue load: idp, authnTime 
		authCtx.setIdentiyProviderID(this.getHeader(
				this.getConfig().getShibIdpID(), request));
		authCtx.setLoginTime(
				this.parseAuthnTime(this.getHeader(
						this.getConfig().getShibAuthnTimeID(), request))); 
		
		log.trace("loaded AuthenticationContext: {}", authCtx.toString());
		this.initSubject(authCtx, request);
	}
	
	
	/**
	 * provides principal/subject with data looked up from given requests shibboleth-SP http header attributes 
	 * {@inheritDoc}
	 */
	@Override
	public Subject initSubject(AuthenticationContext authCtx, HttpServletRequest request) 
	throws AuthenticationException {
		log.trace("initializing subject for {}", authCtx);
		Set<String> targetIDs = this.getConfig().getShibUsernameIDs();
		String shibUsername = this.getFirstHeader(targetIDs, request);
		if(shibUsername == null)
			throw new AuthenticationException("no shib-user found in shib-session, searched IDs " + targetIDs);
		log.trace("found shib-user {}", shibUsername);
		this.addUser(authCtx, shibUsername);	// inits new subject if does not exist yet
		return authCtx.getSubject();
	}
	
	
	private String getHeader(String target, HttpServletRequest request) {
		String result = HttpHeaderUtils.decodeHeaderValue(target, request.getHeader(target));
		if(result != null)
			result = result.trim();
		log.trace("found header {}: {}", target, result);
		return result;
	}
	
	
	private String getFirstHeader(Set<String> targets, HttpServletRequest request) {
		for(String target : targets) {
			Enumeration<?> values = request.getHeaders(target);
			if(values != null) {
				for(Object val ; values.hasMoreElements() ; ) {
					val = values.nextElement();
					if(!(val instanceof String))
						log.warn("found non-string header of type {}", val.getClass().getName());

                                        String result = HttpHeaderUtils.decodeHeaderValue(target, val.toString());
                                        if(result.isEmpty()) {
                                            log.trace("value for header {} is empty.", target);
                                        } else {
                                            log.trace("found header {}: {}", target, result);
                                            return getAttributeValueFromHeader(result);
                                        }
				}
			}
			log.trace("no values found for header {}", target);
		}
		// already returned on first success => nothing found => return null
		return null;
	}
	
        /**
         * Parse the header value by checking if it is multi-valued.
         * If it is not multivalued, just return the header
         * If the values are not equal deny access.
         * 
         * @param header
         * @return 
         */
	private String getAttributeValueFromHeader(String header) {
            if(header.contains(";")) {
                log.trace("multi valued header");
                String[] attributes = header.split(";");
                if(attributes.length < 1) {
                    log.trace("no values found");
                    return null;
                }
                String first = attributes[0];
                for(String attribute : attributes) {
                    if(!first.equalsIgnoreCase(attribute)) {
                        log.warn("Values don't match [{} vs {}]", first, attribute);
                        return null;
                    }
                }
                return first;
            } else {
                log.trace("single valued header");
                return header;
            }
        }
        
	/**
	 * parses the given time string (from shibboleth-SP) into a proper java.util.Date instance
	 * @param time string representation of a timestamp, formatted in shibboleth-SP style
	 * @return 
	 */
	private Date parseAuthnTime(String time) {
		log.trace("parsing (autn)time: {}", time);
		// shib-2.1 provides e.g: "2009-07-30T10:05:51.821Z" 
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date result = null;
		try {
			result = fmt.parse(time);
		} catch(ParseException pE) {
			// just log and continue, what else could be do here...
			log.error("could not parse (shib-sp authn-instant) time: {}", time);
		}
		return result;
	}
}
