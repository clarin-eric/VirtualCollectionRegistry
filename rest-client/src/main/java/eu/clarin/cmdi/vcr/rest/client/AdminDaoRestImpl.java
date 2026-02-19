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
package eu.clarin.cmdi.vcr.rest.client;

import eu.clarin.cmdi.virtualcollectionregistry.core.jwt.JwtUtil;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import jakarta.ws.rs.core.Response;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wilelb
 */
public class AdminDaoRestImpl {
    private final Principal authenticatedUser;
    private final JwtUtil jwtUtil;
    
    public AdminDaoRestImpl(Principal authenticatedUser, JwtUtil jwtUtil) {
        this.authenticatedUser = authenticatedUser;
        this.jwtUtil = jwtUtil;
    }
    
    public List<User> getUsers() {
        Response response = 
            RequestBuilder
                .setPath("admin/users", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get();
        
        String body = response.readEntity(String.class);
        return new ArrayList<>();
        
        //List<User> users = response.readEntity(List<User>.class);
        //return users;
    }
    
    public List<ResourceScan> getResourceScans() {
        Response response = 
            RequestBuilder
                .setPath("admin/scans", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get();
        
        String body = response.readEntity(String.class);
        return new ArrayList<>();
        
        //List<ResourceScan> result = response.readEntity(List<ResourceScan>.class);
        //return result;
    }
}
