package eu.clarin.cmdi.virtualcollectionregistry.core;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import java.util.List;

public interface VirtualCollectionDao {

    User getOrCreateUser(String username) throws VirtualCollectionRegistryException;

    VirtualCollection getVirtualCollection(long id) throws VirtualCollectionRegistryException;
    int getVirtualCollectionCount(QueryFactory factory) throws VirtualCollectionRegistryException;
    List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory factory) throws VirtualCollectionRegistryException;
    List<VirtualCollection> getVirtualCollections(QueryFactory factory) throws VirtualCollectionRegistryException;

    /**
     * Persist the collection into the database. Will create a new collection if the collection has no id set, otherwise
     * it will update the existing instance.
     *
     * @param vc
     * @return 
     * @throws VirtualCollectionRegistryException
     */
    Long persist(VirtualCollection vc) throws VirtualCollectionRegistryException;
    Long delete(Long id) throws VirtualCollectionRegistryException;
    void setVirtualCollectionState(Long id, VirtualCollection.State state) throws VirtualCollectionRegistryException;
    List<String> getOrigins() throws VirtualCollectionRegistryException ;
}
