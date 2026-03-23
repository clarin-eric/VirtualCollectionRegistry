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
package eu.clarin.cmdi.vcr.rest.client;

import eu.clarin.cmdi.virtualcollectionregistry.core.CreatorService;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionDao;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistryDestroyListener;
import eu.clarin.cmdi.virtualcollectionregistry.core.jwt.JwtUtil;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.model.info.Info;
import eu.clarin.cmdi.virtualcollectionregistry.model.rest.ResourceScanFactory;
import eu.clarin.cmdi.virtualcollectionregistry.model.rest.ResourceScanRequest;
import eu.clarin.cmdi.virtualcollectionregistry.model.rest.ResourceScanResponse;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;
        
/**
 *
 * @author wilelb
 */
@Service
public class VirtualCollectionRegistryRestImpl implements VirtualCollectionRegistry, ServletContextAware {

    @Autowired
    private org.springframework.core.env.Environment env;
    
    @Autowired
    private VcrConfig vcrConfig;
    
    private JwtUtil jwtUtil;
    
    @Autowired
    private VirtualCollectionMarshaller marshaller;

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryRestImpl.class);

    private Principal authenticatedUser;
    
    private final List<VirtualCollectionRegistryDestroyListener> destroyListeners = new LinkedList<>();
    
    public VirtualCollectionRegistryRestImpl() {}
    
    public VirtualCollectionRegistryRestImpl(
            VirtualCollectionMarshaller marshaller, 
            VcrConfig vcrConfig) {
        this.marshaller = marshaller;
        this.vcrConfig = vcrConfig;
        this.jwtUtil = new JwtUtil(vcrConfig.getJwtSecret());
    }
    
    @PostConstruct
    public void init() {
        jwtUtil = new JwtUtil(vcrConfig.getJwtSecret());
    }
    
    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    @Override
    public void registerDestroyListener(VirtualCollectionRegistryDestroyListener listener) {
        this.destroyListeners.add(listener);
    }
    
    @Override
    public void setAuthenticatedUser(Principal user) {
        this.authenticatedUser = user;
    }   
    
    @Override
    public Principal getAuthenticatedUser() {
        return this.authenticatedUser;
    }
    
    @Override
    public boolean hasAuthenticatedUser() {
        return this.authenticatedUser != null;
    }
    
//Virtual Collections
    
    @Override
    public VirtualCollectionDao getVirtualCollectionDao() {
        return new VirtualCollectionDaoRestImpl(authenticatedUser, jwtUtil, marshaller);
    }

    @Override
    public long createVirtualCollection(User user, VirtualCollection vc) throws VirtualCollectionRegistryException {
       return getVirtualCollectionDao().persist(vc);
    }

    @Override
    public long createVirtualCollection(Principal principal, VirtualCollection vc) throws VirtualCollectionRegistryException {
        return getVirtualCollectionDao().persist(vc);
    }

    @Override
    public long updateVirtualCollection(Principal principal, long id, VirtualCollection vc) throws VirtualCollectionRegistryException {
        if(vc.getId() == null) {
            throw new VirtualCollectionRegistryException("Cannot update a virtual collection without an id");
        }
        if(vc.getId() != id) {
            throw new VirtualCollectionRegistryException("Cannot update a virtual collection with not matching id ("+id+" != "+vc.getId()+")");
        }
        return getVirtualCollectionDao().persist(vc);
    }

    @Override
    public long deleteVirtualCollection(Principal principal, long id) throws VirtualCollectionRegistryException {
        return getVirtualCollectionDao().delete(id);
    }

    @Override
    public VirtualCollection.State getVirtualCollectionState(long id) throws VirtualCollectionRegistryException {
        VirtualCollection vc = getVirtualCollectionDao().getVirtualCollection(id);
        return vc.getState();
    }

    @Override
    public void setVirtualCollectionState(Principal principal, long id, VirtualCollection.State state) throws VirtualCollectionRegistryException {
        getVirtualCollectionDao().setVirtualCollectionState(id, state);        
    }

    @Override
    public VirtualCollection retrieveVirtualCollection(long id) throws VirtualCollectionRegistryException {
        return getVirtualCollectionDao().getVirtualCollection(id);
    }
    
    @Override
    public List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory qryFactory) throws VirtualCollectionRegistryException {       
        return getVirtualCollectionDao().getVirtualCollections(first, count, qryFactory);
    }

    @Override
    public int getVirtualCollectionCount(QueryFactory qryFactory) throws VirtualCollectionRegistryException {
        return getVirtualCollectionDao().getVirtualCollectionCount(qryFactory);
    }
    
    @Override
    public List<String> getOrigins() throws VirtualCollectionRegistryException {
        logger.debug("getOrigins()");        
        return getVirtualCollectionDao().getOrigins();
    } 

//Users

    @Override
    public User getOrCreateUser(String username) throws VirtualCollectionRegistryException {   
        return getVirtualCollectionDao().getOrCreateUser(username);
    }

    @Override
    public User getOrCreateUser(Principal principal) throws VirtualCollectionRegistryException {
        return getOrCreateUser(principal.getName());
    }

    @Override
    public User fetchUser(Principal principal) throws VirtualCollectionRegistryException {
        return getOrCreateUser(principal.getName());
    }
    
//Info
    
    @Override
    public String getDbVersion() throws VirtualCollectionRegistryException {        
        return RequestBuilder
                .setPath("info", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get(Info.class).getDbVersion();
    }
    
//Resource scanning    

    @Override
    public List<ResourceScan> getAllResourceScans() throws VirtualCollectionRegistryException {
        logger.debug("getAllResourceScans()");
        Response response = 
            RequestBuilder
                .setPath("scans/all", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get();
        
        List<ResourceScanResponse> scansResponse = response.readEntity(new GenericType<List<ResourceScanResponse>>() {});
        return ResourceScanFactory.asResourceScanList(scansResponse);
    }
    
    @Override
    public ResourceScan getResourceScanForRef(String ref) throws VirtualCollectionRegistryException {  
        logger.debug("getAllResourceScans(ref={})", ref);
        List<ResourceScan> filtered = getResourceScansForRefs(List.of(ref));
        if(!filtered.isEmpty()) {
            return filtered.get(0);
        }
        return null;
    }

    @Override
    public List<ResourceScan> getResourceScansForRefs(List<String> refs) throws VirtualCollectionRegistryException {
        logger.debug("getResourceScansForRefs(#refs={})", refs.size());
        Response response = 
            RequestBuilder
                .setPath("scans/filter", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .post(Entity.entity(refs, MediaType.APPLICATION_JSON));
        
        
        List<ResourceScanResponse> scansResponse = response.readEntity(new GenericType<List<ResourceScanResponse>>() {});
        return ResourceScanFactory.asResourceScanList(scansResponse);
    }

    @Override
    public void addResourceScan(String ref, String resolvedRef, String sessionId, boolean useCache) throws VirtualCollectionRegistryException {
        ResourceScanRequest request = new ResourceScanRequest(ref, resolvedRef, sessionId, useCache);
        Response response = 
            RequestBuilder
                .setPath("scans", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));
        if(response.getStatus() != 200) {
            throw new VirtualCollectionRegistryException("Failed to schedule new resource scan. Http response: " + response.getStatus());
        }
    }

    @Override
    public void rescanResource(String ref, String resolvedRef, String sessionId, boolean useCache) throws VirtualCollectionRegistryException {
        ResourceScanRequest request = new ResourceScanRequest(ref, resolvedRef, sessionId, useCache);
        Response response = 
            RequestBuilder
                .setPath("scans", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .put(Entity.entity(request, MediaType.APPLICATION_JSON));
        if(response.getStatus() != 200) {
            throw new VirtualCollectionRegistryException("Failed to re-schedule new resource scan. Http response: " + response.getStatus());
        }
    }
    
//Admin

    @Override
    public List<User> getUsers() {
        return new AdminDaoRestImpl(authenticatedUser, jwtUtil).getUsers();
    }    

    
   
    
 //Not yet implemented   
    
    @Override
    public long newVirtualCollectionVersion(Principal principal, long parentId, VirtualCollection newVersion) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public VirtualCollectionList getVirtualCollections(String query, int offset, int count) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported client side");
    }

    @Override
    public VirtualCollectionList getVirtualCollections(Principal principal, String query, int offset, int count) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported client side");
    }   
    
    @Override
    public CreatorService getCreatorService() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
