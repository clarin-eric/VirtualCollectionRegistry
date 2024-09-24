package de.mpg.aai.shhaa.config;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mpg.aai.shhaa.authz.Location;
import de.mpg.aai.shhaa.authz.LogicRule;
import de.mpg.aai.shhaa.authz.Matcher;
import de.mpg.aai.shhaa.authz.Requirement;
import de.mpg.aai.shhaa.authz.Rule;

/**
 * loads the configuration from the config file
 * @author megger
 *
 */
public class ConfigLoader {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(ConfigContext.class);
	
	
	/**
	 * hidden default constructor 
	 */
	private ConfigLoader() {
	}
	
	/**
	 * reads in and load the configuration from the given config-context (providing the config-file location)
	 * @param configCtx config-context to load on (providing the config-file location)
	 * @return loaded/initialized actual configuration
	 */
	public static Configuration load(ConfigContext configCtx) {
		final Configuration result = new Configuration(configCtx);
		
		try {
			URL location = configCtx.getLocation();
			log.debug("loading config from "+ location);
			DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFact.newDocumentBuilder();
			Document doc = docBuilder.parse(location.toString());
			log.debug("config file parsed sucessfully");

			// load root : by name allows this config included in another parent xml file
			log.trace("loading config root element");
			Element rootElem = (Element)doc.getElementsByTagName("shhaa").item(0);
			if(rootElem == null)
				throw new ConfigurationException("could not find configuration (xml) root-tag");
			
			loadWebApp(result, rootElem);
			loadAuthn(result, rootElem);
			loadHandler(result, rootElem);
			loadComposition(result, rootElem);
			loadAuthz(result, rootElem);
			
			
		} catch(ConfigurationException cE) {	// don't wrap in itself
			throw cE;
		} catch(Exception eE) {
			throw new ConfigurationException("could not load configuration", eE);
		}
		return result;
	}
	
	
	/**
	 * loads the authentication relevant config parameter
	 * @param result configuration instance to put loaded data into
	 * @param rootElem root xml element to read the data from  
	 */
	private static void loadAuthn(final Configuration result, final Element rootElem) {
		Element authn = getElement(rootElem, "authentication", true);
		Element shibHeader = getElement(authn, "shibheader", true);
		NodeList uids = getElements(shibHeader, "username", true);
		for(int ii=0 ; ii < uids.getLength() ; ii++) {
			Element attb = (Element) uids.item(ii);
			result.addShibUsernameID(attb.getTextContent().trim());
		}
		result.setShibSessionID(getValue(shibHeader, "session", false));
		result.setShibIdpID(getValue(shibHeader, "idp", false));
		result.setShibAuthnTimeID(getValue(shibHeader, "timestamp", false));
		
		Element fallback = getElement(authn, "fallback", false); 
		if(fallback != null)
			result.setFallbackUid(getValue(fallback, "username", false));
		
		Element sso = getElement(authn, "sso", false);
		if(sso != null) {
			result.setSSO(sso.getTextContent(), sso.getAttribute("action"));
		}
		Element slo = getElement(authn, "slo", false);
		if(slo != null) {
			result.setSLO(slo.getTextContent(), slo.getAttribute("action"));
		}
	}
	
	
	/**
	 * loads the config parameter relevant for attribute composition/resolving
	 * @param result configuration instance to put loaded data into
	 * @param rootElem root xml element to read the data from  
	 */
	private static void loadComposition(final Configuration result, final Element rootElem) {
		Element resolver = getElement(rootElem, "composition", false);
		if(resolver == null) {
			log.info("no (attributes) composition configuration found, no attribute-based authZ possible");
			return;
		}
		result.setRefreshAction(resolver.getAttribute("action"));
		Element shibHeader = getElement(resolver, "shibheader", false);
		if(shibHeader == null) {
			log.info("no shibheader configuration found");
		} else {
			// if shib header there we expect also some values in it -> mandatory true
			NodeList attributes = getElements(shibHeader, "attribute", true);
			for(int ii=0 ; ii < attributes.getLength() ; ii++) {
				Element attb = (Element) attributes.item(ii);
				result.addTargetAttributeID(attb.getTextContent().trim());
			}
		}
		
		Element jaas = getElement(resolver, "jaas", false);	// optional
		if(jaas != null) {
			result.setJaasConfigName(getValue(jaas, "configname", false));
		}
	}
	
	
	/**
	 * loads the authorization relevant config parameter
	 * @param result configuration instance to put loaded data into
	 * @param rootElem root xml element to read the data from  
	 */
	private static void loadAuthz(final Configuration result, final Element rootElem) {
		Element authz = getElement(rootElem, "authorization", false);
		if(authz == null) {
			log.info("no authorization (for location rules) configuration found");
			return;
		}
		NodeList locations = getElements(authz, "location", false);
		if(locations == null) {
			log.warn("no authorization location rules configuration found, filter does NOT protected any location");
			return;
		}
		for(int ii=0 ; ii < locations.getLength() ; ii++) {
			Element location = (Element) locations.item(ii);
			Location locRule = new Location(
					location.getAttribute("target"), 
					getMatchMode(location),
                                        getMethods(location));
			loadRule(locRule, location);
			result.addLocationRule(locRule);
		}
	}
	
	
	/**
	 * loads the config parameter relevant for the handler behavior
	 * @param result configuration instance to put loaded data into
	 * @param rootElem root xml element to read the data from  
	 */
	private static void loadHandler(final Configuration result, final Element rootElem) {
		Element action = getElement(rootElem, "handler", false);
		if(action == null) {
			log.info("no handler configuration found, using defaults");
			return;
		}
		// handler behavior
		result.setActionParam(getValue(action, "actionparam", false));
		result.setReadOnly(parseBoolean(getValue(action, "readonly", false)));
		// auto-login: default to true if not configured explicitly = standard (shib) use-case
		String val = getValue(action, "autologin", false);
		result.setAutoLogin(val != null ? parseBoolean(val) : true);
		// pages
		Element pages = getElement(action, "pages", false);
		if(pages != null) {
			Element page = getElement(pages, "info", false);
			if(page != null)
				result.setInfo(page.getTextContent(), page.getAttribute("action"));
			page = getElement(pages, "expired", false);
			if(page != null)
				result.setExpired(page.getTextContent(), page.getAttribute("action"));
			page = getElement(pages, "denied", false);
			if(page != null) {
				result.setDenied(page.getTextContent(), page.getAttribute("action"));
			} else
				log.warn("no (manatory) denied-page configured, using default: {}", result.getDeniedPage());
		}
		// ignores
		Element ignores = getElement(action, "ignore", false);
		if(ignores == null)
			return;
		NodeList locations = getElements(ignores, "location", false);
		if(locations == null)
			return;
		for(int ii=0 ; ii < locations.getLength() ; ii++) {
			Element location = (Element) locations.item(ii);
			Location locRule = new Location(
					location.getAttribute("target"), 
					getMatchMode(location),
                                        getMethods(location));
			result.addIgnore(locRule);
		}
	}
	
	
	/**
	 * loads the config parameter about the hosted web application
	 * @param result configuration instance to put loaded data into
	 * @param rootElem root xml element to read the data from  
	 */
	private static void loadWebApp(final Configuration result, final Element rootElem) {
		Element app = getElement(rootElem, "webapp", false);
		if(app == null)
			return;
		result.setHost(getValue(app, "host", false));
		result.setContextPath(getValue(app, "context", false));
		result.setModule(parseBoolean(getValue(app, "module", false)));
	}
	
	
	/**
	 * loads the rules extracted from the given parent element and adds them to the given (parent) target rule
	 * @param target the (parent)target rule to add the extracted rules to
	 * @param parent the xml element to extract the data from 
	 */
	private static void loadRule(final Rule target, Element parent) {
		if(!parent.hasChildNodes())
			return;
		NodeList childs = parent.getChildNodes();
		// failfast and warp into configE
		try {
			for(int ii=0 ; ii < childs.getLength() ; ii++) {
				Node node = childs.item(ii);
				if(!(node instanceof Element))
					continue;
				// else
				Element child = (Element) node;
				String name = child.getTagName();
				if(name.equalsIgnoreCase("rule")) {					// logic-RULE
					Rule logicRule = new LogicRule(child.getAttribute("logic"));
					target.addRule(logicRule);
					loadRule(logicRule, child);	// recurse to load childs
				} else if(name.equalsIgnoreCase("require")) {		// REQUIRE rule
					Rule reqRule = new Requirement(
							child.getAttribute("id"),
							child.getTextContent(),
							getMatchMode(child),
							false);
					target.addRule(reqRule);
				} else if(name.equalsIgnoreCase("miss")) {			// MISS rule
					Rule missRule = new Requirement(
							child.getAttribute("id"),
							child.getTextContent(),
							getMatchMode(child),
							true);
					target.addRule(missRule);
				} else {
					throw new ConfigurationException("found invalid rule tag, expected 'rule', 'require' or 'miss', got  " + name);
				}
			}
		} catch(IllegalArgumentException iaE) {
			throw new ConfigurationException(iaE.getMessage());
		}
	}
	
	
	/**
	 * extracts the match-mode attribute from the given element
	 * @param elem source to extract the data from
	 * @return found value of given elements attribute 'match' 
	 */
	private static String getMatchMode(Element elem) {
		String result = elem.getAttribute("match");
		return result == null || result.isEmpty()
			? Matcher.MATCHMODE_CASE_SENSITIVE
			: result.trim();
	}	
        
        /**
	 * extracts the http methods attribute from the given element
	 * @param elem source to extract the data from
	 * @return found values (attribute value split on " ") of given elements attribute 'methods' 
	 */
	private static String[] getMethods(Element elem) {
		String result = elem.getAttribute("methods");
		return result == null || result.isEmpty()
			? new String[]{}
			: result.split(" ");
	}
	
	/**
	 * parses given string to an boolean 
	 * @param val the string to parse 
	 * @return true if ONLY given value equals(ingore-case) true|1|yes|enabled|on, 
	 * otherwise always false (no IllegalArgumentE)
	 */
	private static boolean parseBoolean(String val) {
		boolean result = false;
		if(val == null || val.isEmpty())
			return result;
		String lc = val.toLowerCase().trim();
		return "true".equals(lc) || "1".equals(lc) || "yes".equals(lc) || "enabled".equals(lc) || "on".equals(lc);
	}
	
	
	/**
	 * provides the NodeList (elements) of all childs with the given tag-name from the given parent element
	 * @param parent the element to lookup the data in
	 * @param tag name of the tag(s) to lookup
	 * @param mandatory flag to indicate whether an result is mandatory (Exception if not found) or not 
	 * @return NodeList representing the found data, null if no hit
	 * @throws ConfigurationException if mandatory and no result could be found
	 */
	private static NodeList getElements(Element parent, String tag, boolean mandatory) {
		NodeList nodes = parent.getElementsByTagName(tag);
		if(mandatory && (nodes == null || nodes.getLength() <= 0))
				throw new ConfigurationException("could not find configuration element-tags " + tag);
		return  nodes;
	}
	/**
	 * provides the (fist) element (of all childs) with the given tag-name from the given parent element
	 * @param parent the element to lookup the data in
	 * @param tag name of the tag(s) to lookup
	 * @param mandatory flag to indicate whether an result is mandatory (Exception if not found) or not 
	 * @return Element representing the (first) found data, null if no hit
	 * @throws ConfigurationException if mandatory and no result could be found
	 */
	private static Element getElement(Element parent, String tag, boolean mandatory) {
		NodeList nodes = parent.getElementsByTagName(tag);
		Element result = (Element) nodes.item(0);
		if(result == null && mandatory)
				throw new ConfigurationException("could not find configuration tag " + tag);
		return  result;
	}
	/**
	 * provides the value of the (fist) element (of all childs) with the given tag-name from the given parent element
	 * @param parent the element to lookup the data in
	 * @param tag name of the tag(s) to lookup
	 * @param mandatory flag to indicate whether an result is mandatory (Exception if not found) or not 
	 * @return VALUE of the element representing the (first) found data, null if no hit
	 * @throws ConfigurationException if mandatory and no result could be found
	 */
	private static String getValue(Element parent, String tag, boolean mandatory) {
		Element target = getElement(parent, tag, mandatory);
		String result = target != null
			? target.getTextContent()
			: null;		// mandatory check for null target already done in #getElement above
		if(result != null)
			result = result.trim();
		if(mandatory && (result == null || result.isEmpty())) {
			throw new ConfigurationException("could not find value in configuration tag " + tag);
		}
		return result;
	}
}
