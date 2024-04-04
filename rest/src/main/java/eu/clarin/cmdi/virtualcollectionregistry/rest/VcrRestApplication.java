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

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/**
 *
 * @author wilelb
 */
@ApplicationPath("/")
public class VcrRestApplication extends ResourceConfig {
     public VcrRestApplication() {
        // Register resources and providers using package-scanning.
        packages("eu.clarin.cmdi.virtualcollectionregistry.rest");

        // Register my custom provider - not needed if it's in my.package.
        //register(SecurityRequestFilter.class);
        // Register an instance of LoggingFilter.
        //register(new LoggingFilter(LOGGER, true));

        // Enable Tracing support.
        property(ServerProperties.TRACING, "ALL");
    }
}
