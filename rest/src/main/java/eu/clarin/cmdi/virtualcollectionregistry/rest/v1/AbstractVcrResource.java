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
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author wilelb
 */
public abstract class AbstractVcrResource {
    @Autowired
    protected VirtualCollectionRegistry registry;
    
    @Autowired
    protected AdminUsersService adminUsersService;
    
    @Context
    protected SecurityContext security;
    
    @Context
    protected UriInfo uriInfo;
    
    @Context
    protected HttpHeaders headers;
    
    @Context
    protected ResourceContext resourceContext;
  
    public AbstractVcrResource() {}
    
    public AbstractVcrResource(
            VirtualCollectionRegistry registry,
            AdminUsersService adminUsersService,
            ResourceContext resourceContext,
            SecurityContext security,
            UriInfo uriInfo,
            HttpHeaders headers) {
        this.registry = registry;
        this.adminUsersService = adminUsersService;
        this.security = security;
        this.uriInfo = uriInfo;
        this.headers = headers;
        this.resourceContext = resourceContext;
    }
    
    protected User getAuthenticatedUser() throws VirtualCollectionRegistryException {
        Principal principal = security.getUserPrincipal();        
        return registry.getOrCreateUser(principal);        
    }
    
    protected boolean isAuthenticatedAdminUser() {
        Principal principal = security.getUserPrincipal();                
        return adminUsersService.isAdmin(principal.getName());
    }
}
