/*
 * Copyright (C) 2026 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.gui;

import eu.clarin.cmdi.virtualcollectionregistry.core.VcrHttpClientFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
@WebListener
public class ApplicationCleanupListener implements ServletContextListener {

    private final static Logger logger = LoggerFactory.getLogger(ApplicationCleanupListener.class);
     
    /**
     * Shutdown logic
     * 
     * @param sce 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Application shutdown");
        VcrHttpClientFactory.shutdown();
    }

    /**
     * Startup logic
     * 
     * @param sce 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Application initialization");
    }
    
}
