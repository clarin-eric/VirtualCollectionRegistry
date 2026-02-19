/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;

/**
 * Load the application via a Jersey Filter definition in web.xml. By using
 * a filter and the jersey ResourceConfig we can override the default Jersey
 * behaviour when a resource is not found and pass it through to the tomcat
 * default servlet that will serve static files with our Swagger UI.
 * 
 * The path must be mapped to /* in the web.xml filter definition.
 * 
 * @author wilelb
 */
public class VcrRestApplication extends ResourceConfig {
    public VcrRestApplication() {
        packages(
            "eu.clarin.cmdi.virtualcollectionregistry.rest", 
            "eu.clarin.cmdi.virtualcollectionregistry.v1"
        );
        
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }
}