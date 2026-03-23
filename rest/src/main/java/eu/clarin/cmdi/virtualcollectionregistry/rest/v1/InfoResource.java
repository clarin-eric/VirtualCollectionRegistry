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
package eu.clarin.cmdi.virtualcollectionregistry.rest.v1;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.info.Info;
import eu.clarin.cmdi.virtualcollectionregistry.rest.auth.ApiKeyRequired;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author wilelb
 */
public class InfoResource extends AbstractVcrResource {
    
    @ApiKeyRequired
    @GET
    @Produces({MediaType.APPLICATION_JSON})
     @Operation(
        summary = "Retrieve basic registry information",
        description = "Retrieve basic registry informatin such as database version and authenticated user")
    public Response getInfo() throws VirtualCollectionRegistryException {
        User user = getAuthenticatedUser();
        return Response.ok(
                new Info(
                    registry.getDbVersion(), 
                    user.getName(), 
                    user.getDisplayName())
            ).build();
    }
}
