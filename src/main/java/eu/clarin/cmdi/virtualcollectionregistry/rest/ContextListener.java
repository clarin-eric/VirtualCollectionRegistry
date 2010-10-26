package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;

public class ContextListener implements ServletContextListener {

    @SuppressWarnings("unchecked")
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        HashMap<String, String> config = new HashMap<String, String>();
        for (Enumeration i = ctx.getInitParameterNames();
             i.hasMoreElements();) {
            String key = (String) i.nextElement();
            String value = ctx.getInitParameter(key);
            if ((value != null) && (value.length() > 0)) {
                config.put(key, value);
            }
        }
        try {
            VirtualCollectionRegistry.initalize(config);
        } catch (VirtualCollectionRegistryException e) {
            ctx.log("error initializing registry", e);
            throw new RuntimeException("error initializing registry", e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        try {
            VirtualCollectionRegistry.instance().destroy();
        } catch (VirtualCollectionRegistryException e) {
            ctx.log("error destroying registry", e);
        }
    }

} // class ContextListener
