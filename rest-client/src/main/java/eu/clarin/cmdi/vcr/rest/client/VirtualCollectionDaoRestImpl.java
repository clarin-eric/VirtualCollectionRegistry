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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionDao;
import eu.clarin.cmdi.virtualcollectionregistry.core.jwt.JwtUtil;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.rest.RestResponse;
import eu.clarin.cmdi.virtualcollectionregistry.rest.utils.RestUtils;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class VirtualCollectionDaoRestImpl implements VirtualCollectionDao {
    
    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionDaoRestImpl.class);
    
    private final Principal authenticatedUser;
    private final JwtUtil jwtUtil;
    private final VirtualCollectionMarshaller marshaller;
            
    public VirtualCollectionDaoRestImpl(Principal authenticatedUser, JwtUtil jwtUtil, VirtualCollectionMarshaller marshaller) {
        this.authenticatedUser = authenticatedUser;
        this.jwtUtil = jwtUtil;
        this.marshaller = marshaller;
    }
    
    //TODO: Move this into a UserDao
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
    public VirtualCollection getVirtualCollection(long id) throws VirtualCollectionRegistryException {
        Response response = 
            RequestBuilder
                .setPath("collections/"+id, jwtUtil)
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

    @Override
    public int getVirtualCollectionCount(QueryFactory factory) throws VirtualCollectionRegistryException {
        Response response = 
            RequestBuilder
                .setPath("collections", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get();
        return getVirtualCollectionsList(response).getVirtualCollections().size();
    }

    @Override
    public List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory factory) throws VirtualCollectionRegistryException {
        Response response = 
            RequestBuilder
                .setPath("collections", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .addParam("offset", first)
                .addParam("count", count)
                .addQueryFactory(factory)
                .get();
        return getVirtualCollectionsList(response).getVirtualCollections();
    }

    @Override
    public List<VirtualCollection> getVirtualCollections(QueryFactory factory) throws VirtualCollectionRegistryException {
        Response response = 
            RequestBuilder
                .setPath("collections", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .get();
        return getVirtualCollectionsList(response).getVirtualCollections();
    }

    @JsonIgnoreProperties
    public static class ApiCreateResponse {
        private Long id;
        private String info;
        private List<String> errors;
        private Boolean success;

        /**
         * @return the id
         */
        public Long getId() {
            return id;
        }

        /**
         * @return the info
         */
        public String getInfo() {
            return info;
        }

        /**
         * @return the errors
         */
        public List<String> getErrors() {
            return errors;
        }

        /**
         * @return the success
         */
        public Boolean getSuccess() {
            return success;
        }

        /**
         * @param id the id to set
         */
        public void setId(Long id) {
            this.id = id;
        }

        /**
         * @param info the info to set
         */
        public void setInfo(String info) {
            this.info = info;
        }

        /**
         * @param errors the errors to set
         */
        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        /**
         * @param success the success to set
         */
        public void setSuccess(Boolean success) {
            this.success = success;
        }
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
    public Long persist(VirtualCollection vc) throws VirtualCollectionRegistryException {
         Response response;         
         if(vc.getId() == null) {
            //Create new collection
            response = 
                RequestBuilder
                    .setPath("collections/", jwtUtil)
                    .setAuthenticatedUser(authenticatedUser)
                    .post(Entity.entity(vc, MediaType.APPLICATION_JSON));
            
            if(response == null) {
                throw new VirtualCollectionRegistryException("No response received");
            }

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new VirtualCollectionRegistryException("Failed to create collection. Http error: response code="+response.getStatus()+", reason="+response.getStatusInfo().getReasonPhrase());
            }
         } else {
            //Update existing collection
            response = 
               RequestBuilder
                   .setPath("virtualcollections/"+vc.getId(), jwtUtil)
                   .setAuthenticatedUser(authenticatedUser)
                   .put(Entity.entity(vc, MediaType.APPLICATION_JSON));
            
            if(response == null) {
                throw new VirtualCollectionRegistryException("No response received");
            }

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new VirtualCollectionRegistryException("Failed to create collection. Http error: response code="+response.getStatus()+", reason="+response.getStatusInfo().getReasonPhrase());
            }
        }
        
        ApiCreateResponse apiResponse = response.readEntity(ApiCreateResponse.class);
        if(!apiResponse.success){
            throw new VirtualCollectionRegistryException("Failed to create collection. Api error: " + apiResponse.errors);
        }

        response.close();
        
        return apiResponse.id;
    }
    
    @Override
    public Long delete(Long id) throws VirtualCollectionRegistryException {
        Response response = 
            RequestBuilder
                .setPath("collections/"+id.toString(), jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .delete();
        
        String body = response.readEntity(String.class);
        logger.info("Body: {}", body);
        
        return -1L;
    }
    
    @Override
    public void setVirtualCollectionState(Long id, VirtualCollection.State state) throws VirtualCollectionRegistryException {
        Response response = 
            RequestBuilder
                .setPath("collections/"+id.toString()+"/state", jwtUtil)
                .setAuthenticatedUser(authenticatedUser)
                .post(Entity.entity(state, MediaType.APPLICATION_JSON));
        
        try {
            RestResponse apiResponse = response.readEntity(RestResponse.class);
            if(!apiResponse.isSuccess()) {
                throw new VirtualCollectionRegistryException("Failed to publish collection. Api error: "+String.join("\n", apiResponse.getErrors()));
            }
        } catch(Exception ex) {
            throw new VirtualCollectionRegistryException("Api communication error", ex);
        }
    }
}
