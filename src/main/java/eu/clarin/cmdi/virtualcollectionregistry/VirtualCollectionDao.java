package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import javax.persistence.EntityManager;
import java.util.List;

public interface VirtualCollectionDao {

    User getOrCreateUser(String username) throws VirtualCollectionRegistryException;

    VirtualCollection getVirtualCollection(long id) throws VirtualCollectionRegistryException;
    int getVirtualCollectionCount(QueryFactory factory) throws VirtualCollectionRegistryException;
    List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory factory);
    List<VirtualCollection> getVirtualCollections(QueryFactory factory);

    /**
     * Persist the collection into the database. Will create a new collection if the collection has no id set, otherwise
     * it will update the existing instance.
     *
     * @param vc
     * @throws VirtualCollectionRegistryException
     */
    void persist(VirtualCollection vc) throws VirtualCollectionRegistryException;

}
