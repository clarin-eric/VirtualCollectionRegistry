package de.mpg.aai.shhaa.resolver;

import de.mpg.aai.shhaa.HttpHeaderUtils;
import java.util.Enumeration;
import java.util.Set;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.config.Configurable;
import de.mpg.aai.shhaa.config.Configuration;
import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.BaseAuthAttribute;


/**
 * implementation of {@link AttributeResolverModule} 
 * to lookup shibboleth attributes from the request http header (shibboleth service provider header attributes)
 * @author megger
 *
 */
public class ShibHeaderAttbResolver implements AttributeResolverModule, Configurable {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(ShibHeaderAttbResolver.class);
	
	private Configuration config;
	
	
	/**
	 * default constructor
	 */
	public ShibHeaderAttbResolver() {
	}
	
	/**
	 * resolves (loads/retrieves/fetches) shibboleth attributes ({@link AuthAttribute}) for the given user
	 * from the given request
	 * and add those as (public) credentials to the given subject 
	 * @param user the subject to look-up the attributes for and add them to
	 * @param request datasource to fetch shibboleth attributes from (http header)
	 * @throws ResolveException if something fails
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void resolve(Subject user, HttpServletRequest request) {
		// skip if no ("further") user attributes required
		Set<String> targets = this.config.getTargetAttributeIDs();
		if(targets == null || targets.isEmpty()) {
			log.info("no shib-attributes configured to resolve.");
			return;
		}
		
		// second: lookup attributes
		log.debug("loading attributes from shib-header...");
		Set<AuthAttribute<String>> credentials = null;
		{	// cast to convenient pointer for destined credential classes
			// expecting (and at worst casting everything as) string attributes only
			Set<?> backedRef = user.getPublicCredentials();
			credentials = (Set<AuthAttribute<String>>) backedRef;
		}
		
		for(String targetID : targets) {
			log.debug("resolving header {} ...", targetID);
			Enumeration<?> values = request.getHeaders(targetID);
			if(values == null || !values.hasMoreElements()) {
				log.debug("no values found for attribute {}", targetID);
				continue;
			}
			log.trace("creating new attribute {}", targetID);
			AuthAttribute<String> cred = new BaseAuthAttribute<String>(targetID);
			for(Object val ; values.hasMoreElements() ; ) {
				val = values.nextElement();
				// shib-header for multi-valued attributes 
				// seem to store it as semicolon separated single string:
				String[] vals = null;
				// expecting just strings => treating everything like one (an exception makes not much sense: no cure)
				// adding string or the found object's string representation
				if(val instanceof String) {
					String[] enc_vals = ((String)val).split(";");	// returns val in size-1 array if no further match 
                                        vals = new String[enc_vals.length];
                                        for(int i = 0; i < enc_vals.length; i++) {
                                            vals[i] = HttpHeaderUtils.decodeHeaderValue(targetID, enc_vals[i]);
                                        }
				} else {
					log.error("found non-string attribute of class '{}', using #toString()", 
							val.getClass().getName());
					// no split necessary here <=> single "value", just wrap in array
					vals = new String[]{val.toString()}; 
				}
				for(String resVal : vals) {
					resVal = resVal.trim();
					if(resVal != null && !resVal.isEmpty()) {
						log.trace("adding to {} value {}", targetID, resVal);
						cred.addValue(resVal);
					}
				}
			}
			if(cred.size() <= 0)
				log.trace("found no values for attribute {}", targetID);
			else 
				credentials.add(cred);
			log.trace("added attribute {} with {} values", targetID, cred.size());
		}
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void setConfig(Configuration conf) {
		this.config = conf;
	}
}
