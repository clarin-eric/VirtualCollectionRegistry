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
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author wilelb
 */
public abstract class AbstractVcrResourceWithMarshaller extends AbstractVcrResource {
    @Autowired
    protected VirtualCollectionMarshaller marshaller;
    
    public AbstractVcrResourceWithMarshaller() {}
    
    public AbstractVcrResourceWithMarshaller(
            VirtualCollectionRegistry registry,
            AdminUsersService adminUsersService,
            VirtualCollectionMarshaller marshaller,
            ResourceContext resourceContext,
            SecurityContext security,
            UriInfo uriInfo,
            HttpHeaders headers) {
        super(registry, adminUsersService, resourceContext, security, uriInfo, headers);
        this.marshaller = marshaller;
    }
}
