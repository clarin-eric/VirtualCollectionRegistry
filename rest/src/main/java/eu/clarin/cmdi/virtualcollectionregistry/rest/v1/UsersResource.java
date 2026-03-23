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
package eu.clarin.cmdi.virtualcollectionregistry.rest.v1;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.rest.auth.ApiKeyRequired;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class UsersResource extends AbstractVcrResource {
   
    private final static Logger logger = LoggerFactory.getLogger(UsersResource.class);

    @Operation(
        summary = "Retrieve a list of all registered users",
        description = "Retrieve a list of all registered users")
    @ApiKeyRequired
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUsers() throws VirtualCollectionRegistryException {  
        if(!isAuthenticatedAdminUser()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        List<User> users = registry.getUsers();
        return Response.ok(users).build();
    }
}
