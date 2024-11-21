package de.mpg.aai.shhaa.config;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigContextListener implements ServletContextListener {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(ConfigContextListener.class);
	
	/** the configuration context */
	private ConfigContext	configCtx;
	
	
	/**
	 * default constructor
	 */
	public ConfigContextListener() {
	}
	
	/** {@inheritDoc} */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		this.closeConfigContext(event.getServletContext());
	}

	/** {@inheritDoc} */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletCtx = event.getServletContext();
		this.initConfigContext(servletCtx);
	}
	
	
	/**
	 * initializes the configuration context and loads the config
	 * @param servletCtx the (web)applications servlet context 
	 */
	private void initConfigContext(ServletContext servletCtx) {
		String location = null;
		try {
			log.debug("init configuration context");
			// first check if already done  
			if(ConfigContext.getActiveConfigContext(servletCtx) != null) 
				throw new IllegalStateException("cannot initialize context because there is already a root application context present - " +
                     "check whether you have multiple ConfigContext* definitions in your web.xml");
			
			// lookup config-file location and init ConfigContext
			String paramName = "ShhaaConfigLocation";
			location = servletCtx.getInitParameter(paramName);
			if(location == null) {
				location = ConfigContext.DEFAULT_CONF_LOCATION;
				log.debug("no config-location found as init-parameter {}, fallback to {}", paramName, location);
			} else
				log.debug("found config-location from init-parameter {}: {}", paramName, location);
			
			URL locURL = location.startsWith("/")
				? servletCtx.getResource(location)
				: new URL(location);
			
			ConfigContext ctx = new ConfigContext();
			ctx.init(locURL);
			this.configCtx = ctx;
			servletCtx.setAttribute(ConfigContext.CONTEXT_ID, this.configCtx);
		} catch(ConfigurationException cE) {
			log.error("failed to initialize configuration context: {}", cE.getMessage());
			throw cE;
		} catch(MalformedURLException muE) {
			throw new ConfigurationException("invalid configuration file location " + location, muE);
		}
	}
	
	
	/**
	 * closes the  configuration context 
	 * and removes it (as attribute) from the given servlet context 
	 * @param servletCtx
	 */
	private void closeConfigContext(ServletContext servletCtx) {
		log.debug("closing configuration context");
		this.configCtx = null;
		servletCtx.removeAttribute(ConfigContext.CONTEXT_ID);
	}
}
