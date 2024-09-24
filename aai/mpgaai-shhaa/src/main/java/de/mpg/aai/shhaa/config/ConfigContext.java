package de.mpg.aai.shhaa.config;

import java.net.URL;

import jakarta.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.HttpAuthService;

/**
 * 
 * holds configuration context data (config file location, the loaded config),
 * invokes loading the config and building the services
 * @author megger
 *
 */
public class ConfigContext {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(ConfigContext.class);
	/** name/identifier to put/get this configurationContext into/from the servletContext */
	public static final String	CONTEXT_ID =	ConfigContext.class.getName();
	/** default location/name of the configuration file */
	public static final String	DEFAULT_CONF_LOCATION =	"/WEB-INF/shhaa.xml";
	/** location/url of the configuration file */
	private URL location;
	
	/** holds the actual (loaded) configuration */
	private Configuration 		config;
	/** the aai authn/z handler */
	private HttpAuthService		authSrv; 
	
	
	/**
	 * default constructor
	 */
	public ConfigContext() {
	}
	
	/**
	 * provides the current/active configuration context (expected/looked-up in the given Servletcontext)
	 * @param servletCtx the (web)applications servlet context
	 * @return the ConfigContext, null if not found
	 */
	public static ConfigContext getActiveConfigContext(ServletContext servletCtx) {
		return (ConfigContext) servletCtx.getAttribute(ConfigContext.CONTEXT_ID);
	}
	
	
	/**
	 * provides the configuration file location (url) 
	 * @return configuration file location (url)
	 */
	public URL getLocation() {
		return this.location;
	}
	/**
	 * sets the configuration file location (url) 
	 * and invokes (re)loading of the configuration
	 * @param url location to/of the configuration file
	 * @see #reload() 
	 */
	public void init(URL url) {
		this.location = url;
		this.reload();
	}
	
	
	/**
	 * (re)loads the configuration from the config file at {@link #location}
	 * AND initializes hereafter the services (authn/z handler)
	 * @see ConfigLoader#load(ConfigContext)
	 * @see ServiceLoader#load(Configuration) 
	 */
	public void reload() {
		log.debug("(re)loading configuration, from location {}", this.location);
		if(this.location == null)
			throw new ConfigurationException("no config file location specified - call init(URL/String) first");
		this.config = ConfigLoader.load(this); 
		this.authSrv = ServiceLoader.load(this);
	}
	
	
	/**
	 * @return the (loaded) actual configuration
	 * @throws ConfigurationException if not found (not loaded yet)
	 */
	public Configuration getConfiguration() throws ConfigurationException {
		if(this.config == null)
			throw new ConfigurationException("no Configuration found - seems not initialized -> call init(URL/String) first");
		return this.config;
	}
	
	
	/**
	 * @return the (loaded/initialized) authn/z handler
	 * @throws ConfigurationException if not found (not loaded yet)
	 */
	public HttpAuthService getAuthService() throws ConfigurationException {
		if(this.authSrv == null)
			throw new ConfigurationException("no authn/z service found - seems not initialized -> call init(URL/String) first");
		return this.authSrv;
	}
}
