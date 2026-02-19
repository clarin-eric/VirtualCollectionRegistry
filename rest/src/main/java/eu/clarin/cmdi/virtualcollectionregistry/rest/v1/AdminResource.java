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

import eu.clarin.cmdi.virtualcollectionregistry.core.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.rest.auth.ApiKeyRequired;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author wilelb
 */
public class AdminResource {
    
    @Autowired
    private AdminUsersService adminUsersService;
    
    @Autowired
    private VirtualCollectionRegistry registry;
    
    @Context
    private SecurityContext security;
    
    @ApiKeyRequired
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/users")
    public Response getUser() throws VirtualCollectionRegistryException {        
        Principal principal = security.getUserPrincipal();        
        if(!adminUsersService.isAdmin(principal.getName())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        List<User> users = registry.getUsers();
        return Response.ok(users).build();
    }
    
    @ApiKeyRequired
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/scans")
    public Response getScans() throws VirtualCollectionRegistryException {        
        Principal principal = security.getUserPrincipal();        
        if(!adminUsersService.isAdmin(principal.getName())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        List<ResourceScan> scans = registry.getAllResourceScans();
        return Response.ok(scans).build();
    }
}
