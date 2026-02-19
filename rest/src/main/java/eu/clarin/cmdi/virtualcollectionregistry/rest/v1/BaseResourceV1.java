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

import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;

/**
 *
 * @author wilelb
 */
public class BaseResourceV1 {
    @Context
    private ResourceContext resourceContext;
    
    @Path("/collections")
    public VirtualCollectionsResource getCollectionsV1() {
        final VirtualCollectionsResource resource =
            resourceContext.getResource(VirtualCollectionsResource.class);
        return resource;
    }

    @Path("/my-virtualcollections")
    public MyVirtualCollectionsResource getMyVirtualCollectionsV1() {
        final MyVirtualCollectionsResource resource = 
            resourceContext.getResource(MyVirtualCollectionsResource.class);
        return resource;
    }
    
    @Path("/info")
    public InfoResource getInfoV1() {
        final InfoResource resource = 
            resourceContext.getResource(InfoResource.class);
        return resource;
    }
    
    @Path("/user")
    public UserResource getUsersV1() {
        final UserResource resource = 
            resourceContext.getResource(UserResource.class);
        return resource;
    }
    
    @Path("/submit")
    public VirtualCollectionFormSubmissionResource getSubmitResource() {
        final VirtualCollectionFormSubmissionResource resource = 
            resourceContext.getResource(VirtualCollectionFormSubmissionResource.class);
        return resource;
    }
    
    @Path("/admin")
    public AdminResource getAdminV1() {
        final AdminResource resource = 
            resourceContext.getResource(AdminResource.class);
        return resource;
    }
}
