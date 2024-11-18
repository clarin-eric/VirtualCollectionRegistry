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
package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.core.CreatorService;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionDao;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistryDestroyListener;
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
//@Service
public class VirtualCollectionRegistryRestImpl {/*implements VirtualCollectionRegistry, InitializingBean, DisposableBean {
    
    @Autowired
    private VirtualCollectionMarshaller marshaller;
    
    //Checks references for validity and gathers additional info for the reference
    @Autowired
    private VirtualCollectionRegistryReferenceValidatorImpl referenceValidator; 
    
    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryRestImpl.class);

    public VirtualCollectionRegistryRestImpl() {}
    
    public VirtualCollectionRegistryRestImpl(VirtualCollectionMarshaller marshaller) {
        this.marshaller = marshaller;
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
            RequestBuilder.setPath("virtualcollections/"+id).get();
        //Response response = getRequest("virtualcollections/"+id).get();
        if(response.getStatus() != 200) {
            throw new VirtualCollectionRegistryException("Invalid api response: HTTP "+response.getStatus()+", "+response.getStatusInfo().getReasonPhrase());
        }
        InputStream input = (InputStream)response.getEntity();      

        VirtualCollection.Format format = RestUtils.getInputFormat(response.getMediaType());
        String encoding = RestUtils.getInputEncoding(response.getMediaType());
        
        VirtualCollection vc = new VirtualCollection();
        try {
            vc = marshaller.unmarshal(input, format, encoding);
        } catch(IOException ex) {
            throw new VirtualCollectionRegistryException("", ex);
        }

        return vc;
    }

    private VirtualCollectionList getVirtualCollectionsList(Response response) throws VirtualCollectionRegistryException {
        InputStream input = (InputStream)response.getEntity();      

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
        Response response = 
            RequestBuilder
                .setPath("virtualcollections")
                .addParam("q", query)
                .addParam("offset", offset)
                .addParam("count", count)
                .get();
        return getVirtualCollectionsList(response);
    }

    @Override
    public VirtualCollectionList getVirtualCollections(Principal principal, String query, int offset, int count) throws VirtualCollectionRegistryException {
        //Principal should be set via authentication
        Response response = RequestBuilder.setPath("my-virtualcollections").get();
        return getVirtualCollectionsList(response);
    }
    
    @Override
    public List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory qryFactory) throws VirtualCollectionRegistryException {
        //qryFactory.
        Response response = RequestBuilder.setPath("virtualcollections").get();
        return getVirtualCollectionsList(response).getVirtualCollections();
    }

    @Override
    public int getVirtualCollectionCount(QueryFactory qryFactory) throws VirtualCollectionRegistryException {
        Response response = RequestBuilder.setPath("virtualcollections").get();
        return getVirtualCollectionsList(response).getVirtualCollections().size();
    }
    
    @Override
    public List<String> getOrigins() throws VirtualCollectionRegistryException {
        //TODO: propery implement
        return new LinkedList<String>();
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    //Admin only
    @Override
    public List<User> getUsers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User getOrCreateUser(String username) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User getOrCreateUser(Principal principal) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CreatorService getCreatorService() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User fetchUser(Principal principal) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDbVersion() throws VirtualCollectionRegistryException {        
        return RequestBuilder
                .setPath("info")
                .get(Info.class).getDbVersion();
    }
    
    
//    private Invocation.Builder getRequest(String path) {
//        WebTarget collectionsWebTarget = webTarget.path(path);        
//        Invocation.Builder invocationBuilder = collectionsWebTarget.request(MediaType.APPLICATION_JSON);
//        return invocationBuilder;
//    }
    
    @Override
    public VirtualCollectionRegistryReferenceValidator getReferenceValidator() {
        return referenceValidator;
    }


    @Override
    public ResourceScan getResourceScanForRef(String ref) throws VirtualCollectionRegistryException {
        ResourceScan scan = RequestBuilder.setPath("scans").get(ResourceScan.class);
        return scan;
    }

    @Override
    public List<ResourceScan> getAllResourceScans() throws VirtualCollectionRegistryException {
        List<ResourceScan> scans = RequestBuilder.setPath("scans").get(List.class);
        return scans;
    }

    @Override
    public List<ResourceScan> getResourceScansForRefs(List<String> refs) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addResourceScan(String ref, String sessionId, boolean useCache) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void rescanResource(String ref, String sessionId, boolean useCache) throws VirtualCollectionRegistryException {
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
*/
}

