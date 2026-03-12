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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionNotFoundException;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.rest.ResourceScanFactory;
import eu.clarin.cmdi.virtualcollectionregistry.model.rest.ResourceScanRequest;
import eu.clarin.cmdi.virtualcollectionregistry.model.rest.ResourceScanResponse;
import eu.clarin.cmdi.virtualcollectionregistry.rest.auth.ApiKeyRequired;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author wilelb
 */
public class ScanResource {
    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionsResource.class);
    
    @Autowired
    private VirtualCollectionRegistry registry;
    @Autowired
    private VirtualCollectionMarshaller marshaller;

    @Context
    private ResourceContext resourceContext;
    @Context
    private SecurityContext security;
    @Context
    private UriInfo uriInfo;
    @Context
    private HttpHeaders headers;
    
    @ApiKeyRequired
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
        summary = "Retrieve a list of all resource scans",
        description = "Retrieve a list of all resource scans")
    public Response getAllScans() throws VirtualCollectionRegistryException {
        List<ResourceScanResponse> response = 
            ResourceScanFactory.asResourceScanResponseList(
                registry.getAllResourceScans()
            );
        return Response.ok(response).build();
    }
    
    @ApiKeyRequired
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
        summary = "Schedule a new resource scan",
        description = "Schedule a new resource scan.")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid API key in "+HttpHeaders.AUTHORIZATION+" header.",
                content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected server side error.",
                content = {@Content(mediaType = "text/html")}
            ),
            @ApiResponse(
                responseCode = "200",
                description = "Scan was scheduled succesfully."
            )
        })
    public Response scanResource(ResourceScanRequest request) throws VirtualCollectionRegistryException {
        registry.addResourceScan(
            request.getRef(), 
            request.getResolvedRef(), 
            request.getSessionId(), 
            request.isUseCache());
        return Response.ok().build();
    }
    
    @ApiKeyRequired
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
        summary = "Schedule a rescan of a resource",
        description = "Schedule a rescan of a resource")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid API key in "+HttpHeaders.AUTHORIZATION+" header.",
                content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected server side error.",
                content = {@Content(mediaType = "text/html")}
            ),
            @ApiResponse(
                responseCode = "200",
                description = "Rescan was scheduled succesfully."
            )
        })
    public Response rescanResource(ResourceScanRequest request) throws VirtualCollectionRegistryException {
        registry.rescanResource(
            request.getRef(), 
            request.getResolvedRef(), 
            request.getSessionId(), 
            request.isUseCache());
        return Response.ok().build();
    }
}
