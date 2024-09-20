package de.mpg.aai.shhaa.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import de.mpg.aai.shhaa.authz.Location;


/**
 * represents the actual configuration, loaded from the config file
 * @see ConfigLoader
 * @see ConfigContext
 * @author megger
 */
public class Configuration {
	/** the logger */
//	private static Logger	log = LoggerFactory.getLogger(Configuration.class);
	/** this configuration's context */
	private ConfigContext	configContext;
	
	// AUTHENTICATION 
	/** set of http-header names/IDs to use/lookup/treat as shibboleth-username */
	private Set<String>	shibUsernameIDs;
	/** http header name/id used/looked-up/treated as shibboleth (service provider) session-ID  */
	private String	shibSessionID;
	/** http header name/id used/looked-up/treated as shibboleth identity-provider(-entity-ID)  */
	private String	shibIdpID;
	/** http header name/id used/looked-up/treated as shibboleth (idp) login-time  */
	private String	shibAuthnTimeID;
	/** username used as fallback when no other (here shibboleth) "real" username could be found  */
	private String	fallbackUid;
	/** URL to the (shibboleth) single-sign-on handler (= SP session initiator) */
	private URL		sso;
	/** "action parameter": request query parameter VALUE used as controller-flag to invoke the SSO cycle
	 * @see #actionParam */
	private String	ssoAction;
	/** URL to the (shibboleth) single-log-out handler (= SP SLO handler) */
	private URL		slo;
	/** "action parameter": request query parameter VALUE used as controller-flag to invoke the SLO cycle 
	 * @see #actionParam */
	private String	sloAction;
	
	// RESOLVER
	/** set of IDs/names of the properties/attributes to be looked-up/used as the destined principal's attributes */
	private Set<String>	targetAttributeIDs;
	/** name of the jaas config file SECTION where to lookup the jaas config 
	 * (for "plugged-in" additional jaas-based (user) attribute composition/resolving
	 */
	private String		jaasConfigName;
	
	// AUTHORIZATION
	/** loaded rules for authorization */
	private List<Location>	authzLocationRules;
	
	// HANDLER
	/** name of the request parameter used as controller flag to invoke events (like login/logout);
	 * defaults to "shhaaDo" */
	private String		actionParam;
	/** "action parameter": request query parameter VALUE used as controller-flag to invoke attribute-refresh (reload attributes) 
	 * @see #actionParam */
	private String		refreshAction;
	// pages	[action, page]
	/** holds configured (access-)denied [action, page]
	 * @see #actionParam */
	private String[]	denied;
	/** holds configured session-expired/logged-out [action, page]
	 * @see #actionParam */
	private String[]	expired;
	/** holds configured (new)login-/session-info [action, page]
	 * @see #actionParam */
	private String[]	info;
	// handler behavior
	/** flag to control status of provided user Subject, if true Subject is set to readOnly*/
	private boolean		readOnly;
	/** flag to control handler auto login behavior: if true handler will immediately trigger the redirect to sso login, 
	 * on false it will just handle to access-denied, default true */
	private boolean		autoLogin = true;
	/** list of locations to be ignored/not-treated by the authn/z handler */
	private List<Location>	ignores;
	
	// WEBAPP
	/** the hostname by which the protected webapp is accessed */
	private URL			host;
	/** the context-path by which the protected webapp is accessed */
	private String		contextPath;
	/** 
	 * flag to control initial login info behavior:
	 * is the webapp of this filter "just" a module of another app?  
	 * if true: NO login-info is displayed on first/initial-access even with valid (shib) login;
	 */
	private boolean		module;

	
	/**
	 * constructor, initializes this config's context
	 */
	public Configuration(ConfigContext ctx) {
		this.configContext = ctx;
	}
	
	
	/**
	 * provides this Configuration's Context 
	 * @return ConfigContext
	 */
	public ConfigContext getContext() {
		return this.configContext;
	}
	
	/**
	 * @return {@link #shibUsernameIDs}
	 */
	public Set<String> getShibUsernameIDs() {
		return this.shibUsernameIDs;
	}
	/**
	 * adds given value to {@link #shibUsernameIDs}
	 * @param value
	 */
	public void addShibUsernameID(String value) {
		String val = this.ensureVal(value);
		if(val == null)
			throw new IllegalArgumentException("shib-username-id must have some value");
		if(this.shibUsernameIDs == null)
			this.shibUsernameIDs = Collections.synchronizedSet(new LinkedHashSet<String>());
		this.shibUsernameIDs.add(val);
	}
	
	/**
	 * @return {@link #shibSessionID}
	 */
	public String getShibSessionID() {
		return this.shibSessionID != null
			? this.shibSessionID
			: "Shib-Session-ID";
	}
	/**
	 * @param value sets {@link #shibSessionID}
	 */
	public void setShibSessionID(String value) {
		this.shibSessionID = this.ensureVal(value); 
	}
	
	/**
	 * @return {@link #shibIdpID}
	 */
	public String getShibIdpID() {
		return this.shibIdpID != null
			? this.shibIdpID
			: "Shib-Identity-Provider";
	}
	/**
	 * @param value sets {@link #shibIdpID}
	 */
	public void setShibIdpID(String  value) {
		this.shibIdpID = this.ensureVal(value); 
	}
	
	/**
	 * @return {@link #shibAuthnTimeID}
	 */
	public String getShibAuthnTimeID() {
		return this.shibAuthnTimeID != null
			? this.shibAuthnTimeID
			: "Shib-Authentication-Instant";
	}
	/**
	 * @param value sets {@link #shibAuthnTimeID}
	 */
	public void setShibAuthnTimeID(String value) {
		this.shibAuthnTimeID = this.ensureVal(value); 
	}
	
	/**
	 * @return {@link #fallbackUid}
	 */
	public String getFallbackUid() {
		return this.fallbackUid != null 
			? this.fallbackUid
			: "anonymous";
	}
	/**
	 * @param value sets {@link #fallbackUid}
	 */
	public void setFallbackUid(String value) {
		this.fallbackUid = this.ensureVal(value);
	}
	
	
	/**
	 * @return {@link #targetAttributeIDs}
	 */
	public Set<String> getTargetAttributeIDs() {
		return this.targetAttributeIDs;
	}
	/**
	 * @param value added to {@link #targetAttributeIDs}
	 */
	public void addTargetAttributeID(String value) {
		String val = this.ensureVal(value);
		if(val == null)
			throw new IllegalArgumentException("target-attribute-id must have a value");
		if(this.targetAttributeIDs == null)
			this.targetAttributeIDs = Collections.synchronizedSet(new LinkedHashSet<String>());
		this.targetAttributeIDs.add(val);
	}
	
	/**
	 * @return {@link #jaasConfigName}
	 */
	public String getJaasConfigName() {
		return this.jaasConfigName;
	}
	/**
	 * @param value sets {@link #jaasConfigName}
	 */
	public void setJaasConfigName(String value) {
		this.jaasConfigName = value;
	}
	
	/**
	 * @return {@link #readOnly}
	 */
	public boolean isReadOnly() {
		return this.readOnly;
	}
	/**
	 * @param val sets {@link #readOnly}
	 */
	public void setReadOnly(boolean val) {
		this.readOnly = val;
	}
	
	/**
	 * @return {@link #autoLogin}
	 */
	public boolean isAutoLogin() {
		return this.autoLogin;
	}
	/**
	 * @param val sets {@link #autoLogin}
	 */
	public void setAutoLogin(boolean val) {
		this.autoLogin = val;
	}
	
	
	/**
	 * @return {@link #host}
	 */
	public URL getHost() {
		return this.host;
	}
        
	/**
	 * @param hostname sets {@link #host}
	 */
	public void setHost(String hostname) {
		if(hostname == null || hostname.isEmpty()) {
			this.host = null;
			return;
		}
		try {
			this.host = new URL(hostname);
		} catch (MalformedURLException muE) {
			throw new ConfigurationException("failed to set hostname", muE);
		}

	}
	
	/**
	 * @return {@link #contextPath}
	 */
	public String getcontextPath() {
		return this.contextPath;
	}
	/**
	 * @param webappCtxPath sets {@link #contextPath}
	 */
	public void setContextPath(String webappCtxPath) {
		if(webappCtxPath != null) {
			if(!webappCtxPath.startsWith("/"))
				throw new ConfigurationException("got invalid (web-app) context-path, path must start with '/'");
//			if(!webappCtxPath.endsWith("/"))
//				log.warn("context path does not end with '/'");
		}
		this.contextPath = webappCtxPath;
	}
	
	/**
	 * @return {@link #module}
	 */
	public boolean isModule() {
		return this.module;
	}
	/**
	 * @param val sets {@link #module}
	 */
	public void setModule(boolean val) {
		this.module = val;
	}
	
	
	/**
	 * @return {@link #actionParam}
	 */
	public String getActionParam() {
		return this.actionParam != null
			? this.actionParam
			: "shhaaDo";
	}
	/**
	 * @param value sets {@link #actionParam}
	 */
	public void setActionParam(String value) {
		this.actionParam = this.ensureVal(value); 
	}
	
	/**
	 * @return {@link #refreshAction}
	 */
	public String getRefeshAction() {
		// functionality always present <=> provide default in case
		return this.refreshAction != null 
			? this.refreshAction
			: "rF";
	}
	/**
	 * @param value sets {@link #refreshAction}
	 */
	public void setRefreshAction(String value) {
		this.refreshAction = this.ensureVal(value);
	}
	
	/**
	 * @return {@link #ssoAction} IF {@link #sso} exists
	 * @see #actionParam
	 */
	public String getSsoAction() {
		// action ONLY if there is an url
		// but then action must exist, fallback to default in case
		return this.sso!= null
			? this.ssoAction != null
				? this.ssoAction
				: "sso"
			: null;
	}
	/**
	 * @return {@link #sso}
	 */
	public URL getSSO() {
		return this.sso;
	}
	/**
	 * sets {@link #sso} and {@link #ssoAction}
	 * @param url the SSO url to set
	 * @param action the SSO action parameter value to set 
	 * @throws ConfigurationException if given url is invalid
	 * @see #actionParam
	 */
	public void setSSO(String url, String action)  throws ConfigurationException {
		try {
			this.sso = new URL(url);
			this.ssoAction = this.ensureVal(action);
		} catch (MalformedURLException muE) {
			throw new ConfigurationException("failed to set SSO-url", muE);
		}
	}
	
	
	/**
	 * @return {@link #sloAction} IF {@link #slo} exists
	 * @see #actionParam
	 */
	public String getSloAction() {
		// action ONLY if there is an url
		// but then action must exist, fallback to default in case
		return this.slo != null
			? this.sloAction != null
				? this.sloAction
				: "slo"
			: null;
	}
	/**
	 * @return {@link #slo}
	 */
	public URL getSLO() {
		return this.slo;
	}
	/**
	 * sets {@link #slo} and {@link #sloAction}
	 * @param url the url to set
	 * @param action the action parameter value to set
	 * @throws ConfigurationException
	 * @see #actionParam
	 */
	public void setSLO(String url, String action)  throws ConfigurationException {
		try {
			this.slo = new URL(url);
			this.sloAction = this.ensureVal(action);
		} catch (MalformedURLException muE) {
			throw new ConfigurationException("failed to set SLO-url", muE);
		}
	}
	
	/**
	 * @return {@link #denied}[0]  IF page {@link #denied}[1] exists 
	 * @see #actionParam
	 */
	public String getDeniedAction() {
		// action ONLY makes sense if there is a page, too
		//	(and page must be *explicitly* configured (not just default)) 
		return this.denied == null || this.denied[1] == null
			? null		// null allowed <=> no default
			: this.denied[0];
	}
	/**
	 * @return {@link #denied}[1], default "pages/noaccess.jsp"
	 */
	public String getDeniedPage() {
		return this.denied == null || this.denied[1] == null 
			? "pages/noaccess.jsp"	// default, never null
			: this.denied[1];
	}
	/**
	 * sets {@link #denied}
	 * @param page the reroute target
	 * @param action request query parameter value
	 * @see #actionParam
	 */
	public void setDenied(String page, String action) {
		this.denied = new String[2];	// [action, page]
		this.denied[0] = this.ensureVal(action);
		this.denied[1] = this.ensureVal(page);
	}
	
	/**
	 * @return {@link #expired}[1]
	 */
	public String getExipredPage() {
		return this.expired != null 
			? this.expired[1]
			: null;		// null allowed <=> no default
	}
	/**
	 * @return {@link #expired}[0]  IF page {@link #expired}[1] exists 
	 */
	public String getExpiredAction() {
		// action ONLY makes sense if there is a page, too
		return this.expired == null || this.expired[1] == null
			? null		// null allowed <=> no default
			: this.expired[0];
	}
	/**
	 * sets {@link #expired} 
	 * @param page the reroute target 
	 * @param action request query parameter value
	 */
	public void setExpired(String page, String action) {
		this.expired = new String[2];	// [action, page]
		this.expired[0] = this.ensureVal(action);
		this.expired[1] = this.ensureVal(page);
	}
	
	/**
	 * @return {@link #info}[1]
	 */
	public String getInfoPage() {
		return this.info != null 
			? this.info[1]
			: null;		// null allowed <=> no default
	}
	/**
	 * @return {@link #info}[0]  IF page {@link #info}[1] exists
	 */
	public String getInfoAction() {
		// action ONLY makes sense if there is a page, too
		return this.info == null || this.info[1] == null
			? null		// null allowed <=> no default
			: this.info[0];
	}
	/**
	 * sets {@link #info}
	 * @param page the reroute target
	 * @param action request query parameter value
	 */
	public void setInfo(String page, String action) {
		this.info = new String[2];	// [action, page]
		this.info[0] = this.ensureVal(action);
		this.info[1] = this.ensureVal(page); 
	}
	
	/**
	 * @return {@link #authzLocationRules}
	 */
	public List<Location> getLocationRules() {
		return this.authzLocationRules;
	}
	/**
	 * adds given rule to {@link #authzLocationRules} 
	 * @param rule the Location(rule) to add
	 */
	public void addLocationRule(Location rule) {
		if(this.authzLocationRules == null)
			this.authzLocationRules = Collections.synchronizedList(new Vector<Location>());
		this.authzLocationRules.add(rule);
	}
	/**
	 * @return {@link #ignores}
	 */
	public List<Location> getIgnores() {
		return this.ignores;
	}
	/**
	 * adds given location to {@link #ignores} 
	 * @param rule Location(rule) to add
	 */
	public void addIgnore(Location rule) {
		if(this.ignores == null)
			this.ignores = Collections.synchronizedList(new Vector<Location>());
		this.ignores.add(rule);
	}
	
	
	/**
	 * checks the given string value and makes sure it has a valid - non-empty, trimmed - textual content
	 * @param value the string to check
	 * @return null if given value is null, empty or contains only whitespaces, otherwise the trimmed text content
	 */
	private String ensureVal(String value) {
		 return value == null 
			? null
			: value.trim().isEmpty() 
				? null
				: value.trim();
	}
}
