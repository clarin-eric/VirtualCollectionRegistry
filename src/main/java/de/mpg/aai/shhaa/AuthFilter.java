package de.mpg.aai.shhaa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.aai.shhaa.config.ConfigContext;
import de.mpg.aai.shhaa.config.ConfigContextListener;
import de.mpg.aai.shhaa.config.ConfigurationException;
import de.mpg.aai.shhaa.context.AuthenticationContext;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;

/**
 * servlet filter implementation,
 * calls the main service, aai authn/z handler {@link HttpAuthService#handleAuth(HttpServletRequest, HttpServletResponse)}
 * and continues/forwards/redirects the original request to the proper location depending on the returned result
 * @see HttpAuthService
 * @see AAIServletRequest
 * @author megger
 *
 */
public class AuthFilter implements Filter {
	/** the logger */
	private static Logger	log = LoggerFactory.getLogger(HttpAuthService.class);
	/** the aai authn/z handler */
	private HttpAuthService		authSrv; 
	
	
	/**
	 * default constructor 
	 */
	public AuthFilter() {
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void destroy() {
		this.authSrv = null;
	}
	
	
	/** 
	 * calls the main service, aai authn/z handler {@link HttpAuthService#handleAuth(HttpServletRequest, HttpServletResponse)}
	 * and continues/forwards/redirects the original request to the proper location depending on the returned result:
	 * if returned {@link AuthenticationContext}'s target starts with http the resulted request is redirected to the given url, 
	 * otherwise it's forwarded (dispatched);
	 * on no specific target (null) it continues with the filter-chain.  
	 * <div>{@inheritDoc}</div> */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		log.trace("start aai auth filtering...");

                String default_encoding = Charset.defaultCharset().name();
                if(req.getCharacterEncoding() == null) {
                    log.debug("Client did not specify character encoding. Using {} as default.", default_encoding);
                    req.setCharacterEncoding(default_encoding);
                } else {
                    log.debug("Client specified {} as encoding.", req.getCharacterEncoding());
                }
                
		if(!(req instanceof HttpServletRequest))
			throw new IllegalArgumentException("expecting HttpServletRequest, found "+ req.getClass().getName());
		HttpServletRequest request = (HttpServletRequest) req;
		if(!(resp instanceof HttpServletResponse))
			throw new IllegalArgumentException("expecting HttpServletResponse, found "+ resp.getClass().getName());
		HttpServletResponse response = (HttpServletResponse) resp;
		
		AAIServletRequest result = this.authSrv.handleAuth(request, response);
		if(result == null) {
			log.trace("ignored aai authn/z handling, continue filter chain.");
			chain.doFilter(request, response);
			return;
		}
		
		String target= result.getTarget();
		if(target == null) {
			log.trace("aai authn/z handling done, continue filter chain.");
			chain.doFilter(result, response);
			return;
		}
		// else: target found => handle forward/redirect 
		log.trace("aai authn/z handling done, found target {}", target);
		if(target.startsWith("http")) {		// redirect
			log.trace("sending redirect {}.", target);
			response.sendRedirect(target);
		} else {	// forward
			RequestDispatcher dispatch = request.getRequestDispatcher(target);
			if(dispatch == null)
				throw new NullPointerException("could not acquire dispatcher from request to forward to target " + target);
			log.trace("dispatch forward to {}.", target);
			dispatch.forward(result, response);
		}
	}
	
	
	/** 
	 * initializes the aai authn/z handler {@link #authSrv}:
	 * first tries to lookup config from the servlet-context,
	 * put there by {@link ConfigContextListener}, 
	 * if not found it tries to load the config from this filter's init parameter ({@link #loadConfig(FilterConfig)}).
	 * <div>{@inheritDoc}</div> 
	 * @see ConfigContextListener#contextInitialized(jakarta.servlet.ServletContextEvent)
	 * @see {@link ConfigContextListener#initConfigContext(ServletContext)
	 */
	@Override
	public void init(FilterConfig conf) throws ServletException {
		log.trace("init aai auth filter");
		
		// first check servlet context, possibly put there by context-listener
		ServletContext servletCtx = conf.getServletContext();
		ConfigContext confCtx = ConfigContext.getActiveConfigContext(servletCtx);
		
		// not in servlet context => load here
		if(confCtx == null) {
			try {
				confCtx = this.loadConfig(conf);
			} catch(ConfigurationException cE) {
				log.error("failed to initialize configuration context: {}", cE.getMessage());
				UnavailableException uE = new UnavailableException("could not load configuration context.");
				uE.initCause(cE);
				throw uE;
			}
		}
		if(confCtx == null)
			throw new UnavailableException("found no configuration context.");
		this.authSrv  = confCtx.getAuthService();
		if(this.authSrv == null)
			throw new UnavailableException("found no HttpAuthService in configuration context.");
	}
	
	/**
	 * loads the aai-authn/z handler configuration from the filter init parameter 
	 * @param conf the filter configuration 
	 * @return the configuration context
	 * @throws ConfigurationException if something failed during initialization/loading of the configuartion
	 */
	private ConfigContext loadConfig(FilterConfig conf) throws ConfigurationException {
		String location = null;
		try {
			// lookup config-file location and init ConfigContext
			String paramName = "ConfigLocation";
			location = conf.getInitParameter(paramName);
			if(location == null) {
				location = ConfigContext.DEFAULT_CONF_LOCATION;
				log.debug("no config-location found as init-parameter {}, fallback to {}", paramName, location);
			} else
				log.debug("found config-location from init-parameter {}: {}", paramName, location);
			
			ServletContext servletCtx = conf.getServletContext();
			URL locURL = location.startsWith("/")
				? servletCtx.getResource(location)
				: new URL(location);
				
			ConfigContext ctx = new ConfigContext();
			ctx.init(locURL);
			return ctx;
		} catch(MalformedURLException muE) {
			throw new ConfigurationException("invalid configuration file location " + location, muE);
		}
	}

}
