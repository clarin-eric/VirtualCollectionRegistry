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
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.VirtualCollectionRegistryReferenceValidator;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.VirtualCollectionRegistryReferenceValidatorImpl;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.model.info.Info;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.rest.utils.RestUtils;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
        
/**
 *
 * @author wilelb
 */
@Service
public class VirtualCollectionRegistryRestImpl implements VirtualCollectionRegistry, InitializingBean, DisposableBean {

    private final Client client = ClientBuilder.newClient();
    
    private final String secret = "ghekjghejkhgjkehjkfhgcilqu;ioqhq!;oijf;qhvbwl";
    private final JwtUtil jwtUtil;
    
    @Autowired
    private VirtualCollectionMarshaller marshaller;
    
    //Checks references for validity and gathers additional info for the reference
    @Autowired
    private VirtualCollectionRegistryReferenceValidatorImpl referenceValidator; 
    
    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryRestImpl.class);

    private Principal authenticatedUser;
    
    public VirtualCollectionRegistryRestImpl() {
        this.jwtUtil = new JwtUtil(secret);
    }
    
    public VirtualCollectionRegistryRestImpl(VirtualCollectionMarshaller marshaller) {
        this.marshaller = marshaller;
        this.jwtUtil = new JwtUtil(secret);
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
    
    @Override
    public VirtualCollectionDao getVirtualCollectionDao() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long createVirtualCollection(User user, VirtualCollection vc) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long createVirtualCollection(Principal principal, VirtualCollection vc) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long updateVirtualCollection(Principal principal, long id, VirtualCollection vc) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long deleteVirtualCollection(Principal principal, long id) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long newVirtualCollectionVersion(Principal principal, long parentId, VirtualCollection newVersion) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VirtualCollection.State getVirtualCollectionState(long id) throws VirtualCollectionRegistryException {
        VirtualCollection vc = getCollection(id);
        return vc.getState();
    }

    @Override
    public void setVirtualCollectionState(Principal principal, long id, VirtualCollection.State state) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VirtualCollection retrieveVirtualCollection(long id) throws VirtualCollectionRegistryException {
        return getCollection(id);
    }
    
    private VirtualCollection getCollection(long id) throws VirtualCollectionRegistryException {
        Response response = 
            RequestBuilder
                .setPath("virtualcollections/"+id, jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get();
        
        if(response.getStatus() != 200) {
            throw new VirtualCollectionRegistryException("Invalid api response: HTTP "+response.getStatus()+", "+response.getStatusInfo().getReasonPhrase());
        }
        
        InputStream input = (InputStream)response.getEntity();      

        VirtualCollection.Format format = RestUtils.getInputFormat(response.getMediaType());
        String encoding = RestUtils.getInputEncoding(response.getMediaType());
        
        VirtualCollection vc = new VirtualCollection();
        try {
            vc = marshaller.unmarshal(input, format, encoding);
            //Note: this is a workaround to fix the client authorization check
            //      The rest api checks for permission. If it returns a collection
            //      the authentication user is allowed to access it.
            //      We explicitly set the owner here based on the user principal
            //      that made the request.
            vc.setOwner(new User(authenticatedUser));
        } catch(IOException ex) {
            logger.error("Failed to unmarshall collection", ex);
            throw new VirtualCollectionRegistryException("", ex);
        }

        return vc;
    }

    private VirtualCollectionList getVirtualCollectionsList(Response response) throws VirtualCollectionRegistryException {
        InputStream input = (InputStream)response.getEntity();      

        if(response.getStatus() != 200) {
            throw new VirtualCollectionRegistryException("Unexpected response code: "+response.getStatus() +", "+response.getStatusInfo().getReasonPhrase());
        }
        
        VirtualCollection.Format format = RestUtils.getInputFormat(response.getMediaType());
        String encoding = RestUtils.getInputEncoding(response.getMediaType());
        
        VirtualCollectionList list = new VirtualCollectionList();
        try {
            list = marshaller.unmarshalCollectionList(input, format, encoding);
        } catch(IOException ex) {
            throw new VirtualCollectionRegistryException("", ex);
        }
        return list;
    }
    
    @Override
    public VirtualCollectionList getVirtualCollections(String query, int offset, int count) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported client side");
        /*
        Response response = 
            RequestBuilder
                .setPath("virtualcollections")
                .addParam("q", query)
                .addParam("offset", offset)
                .addParam("count", count)
                .get();
        return getVirtualCollectionsList(response);
        */
    }

    @Override
    public VirtualCollectionList getVirtualCollections(Principal principal, String query, int offset, int count) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported client side");
        /*
        //Principal should be set via authentication
        Response response = 
            RequestBuilder
                .setPath("my-virtualcollections")
                .addParam("q", query)
                .addParam("offset", offset)
                .addParam("count", count)
                .get();
        return getVirtualCollectionsList(response);
        */
    }
    
    @Override
    public List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory qryFactory) throws VirtualCollectionRegistryException {       
        Response response = 
            RequestBuilder
                .setPath("virtualcollections", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .addParam("offset", first)
                .addParam("count", count)
                .addQueryFactory(qryFactory)
                .get();
        return getVirtualCollectionsList(response).getVirtualCollections();
    }

    @Override
    public int getVirtualCollectionCount(QueryFactory qryFactory) throws VirtualCollectionRegistryException {
        Response response = 
            RequestBuilder
                .setPath("virtualcollections", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get();
        return getVirtualCollectionsList(response).getVirtualCollections().size();
    }
    
    @Override
    public List<String> getOrigins() throws VirtualCollectionRegistryException {
        //TODO: propery implement
        logger.error("getOrigins() not implemented yet");
        return new LinkedList<>();
    }

    //Admin only
    @Override
    public List<User> getUsers() {
        logger.error("getUsers() not implemented yet");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User getOrCreateUser(String username) throws VirtualCollectionRegistryException {   
        Response response = 
            RequestBuilder
                .setPath("user", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get();
        String usernameResponse = response.readEntity(String.class);
        return new User(usernameResponse);
    }

    @Override
    public User getOrCreateUser(Principal principal) throws VirtualCollectionRegistryException {
        return getOrCreateUser(principal.getName());
    }

    @Override
    public User fetchUser(Principal principal) throws VirtualCollectionRegistryException {
        return getOrCreateUser(principal.getName());
    }
    
    @Override
    public String getDbVersion() throws VirtualCollectionRegistryException {        
        return RequestBuilder
                .setPath("info", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get(Info.class).getDbVersion();
    }
    
    @Override
    public VirtualCollectionRegistryReferenceValidator getReferenceValidator() {
        return referenceValidator;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Override
    public CreatorService getCreatorService() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResourceScan getResourceScanForRef(String ref) throws VirtualCollectionRegistryException {
        ResourceScan scan = RequestBuilder.setPath("scans", jwtUtil).get(ResourceScan.class);
        return scan;
    }

    @Override
    public List<ResourceScan> getAllResourceScans() throws VirtualCollectionRegistryException {
        List<ResourceScan> scans = RequestBuilder.setPath("scans", jwtUtil).get(List.class);
        return scans;
    }

    @Override
    public List<ResourceScan> getResourceScansForRefs(List<String> refs) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void registerDestroyListener(VirtualCollectionRegistryDestroyListener listener) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void destroy() throws Exception {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void addResourceScan(String ref, String resolvedRef, String sessionId, boolean useCache) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void rescanResource(String ref, String resolvedRef, String sessionId, boolean useCache) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
