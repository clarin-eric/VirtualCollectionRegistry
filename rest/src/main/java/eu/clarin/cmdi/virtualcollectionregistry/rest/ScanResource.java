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

import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  ResourceScan REST resource
 * 
 * @author wilelb
 */
public class ScanResource {
   
    @Autowired
    private VirtualCollectionRegistry registry;
    
    @Context
    private SecurityContext security;
    
    protected ScanResource(VirtualCollectionRegistry registry, SecurityContext security) {
        this.registry = registry;
        this.security = security;
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllResourceScans() throws VirtualCollectionRegistryException {
        List<ResourceScan> scans = registry.getAllResourceScans();
        return Response.ok(scans).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getResourceScan(@PathParam("ref") String ref) throws VirtualCollectionRegistryException {
        ResourceScan scan = registry.getResourceScanForRef(ref);
        return Response.ok(scan).build();
    }
    
    @POST
    public Response addResourceScan(@PathParam("ref") String ref, @PathParam("actionable_ref") String actionableRef, @PathParam("session") String session, @PathParam("cache") boolean useCache) throws VirtualCollectionRegistryException {        
        registry.addResourceScan(ref, actionableRef, session, useCache);
        return Response.ok().build();
    }
    
    @PUT
    public Response updateResourceScan(@PathParam("ref") String ref, @PathParam("actionable_ref") String actionableRef, @PathParam("session") String session, @PathParam("cache") boolean useCache) throws VirtualCollectionRegistryException {        
        registry.rescanResource(ref, actionableRef, session, useCache);
        return Response.ok().build();
    }
}
