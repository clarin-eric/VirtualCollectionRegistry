package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD;

import javax.persistence.EntityManager;
import java.security.Principal;
import java.util.List;

public interface VirtualCollectionRegistry {

    /**
     * Will store the specified collection; it will also set the owner according
     * to the specified principal and set its state to
     * {@link VirtualCollection.State#PRIVATE}
     *
     * @param principal owner principal
     * @param vc collection to store
     * @return identifier of the persisted collection
     * @throws VirtualCollectionRegistryException
     */
    long createVirtualCollection(User user, VirtualCollection vc) throws VirtualCollectionRegistryException;

    long createVirtualCollection(Principal principal,
            VirtualCollection vc) throws VirtualCollectionRegistryException;

    long updateVirtualCollection(Principal principal, long id,
            VirtualCollection vc) throws VirtualCollectionRegistryException;

    long deleteVirtualCollection(Principal principal, long id)
            throws VirtualCollectionRegistryException;

    long newVirtualCollectionVersion(Principal principal, long parentId,
            VirtualCollection newVersion) throws VirtualCollectionRegistryException;

    VirtualCollection.State getVirtualCollectionState(long id)
            throws VirtualCollectionRegistryException;

    void setVirtualCollectionState(Principal principal, long id,
            VirtualCollection.State state)
            throws VirtualCollectionRegistryException;

    /**
     *
     * @param id identifier of the virtual collection to retrieve
     * @return the identified virtual collection, never null
     * @throws VirtualCollectionRegistryException if no virtual collection with
     * the specified identifier exists
     */
    VirtualCollection retrieveVirtualCollection(long id)
            throws VirtualCollectionRegistryException;

    VirtualCollectionList getVirtualCollections(String query,
            int offset, int count) throws VirtualCollectionRegistryException;

    VirtualCollectionList getVirtualCollections(Principal principal,
            String query, int offset, int count)
            throws VirtualCollectionRegistryException;

    int getVirtualCollectionCount(QueryFactory qryFactory) throws VirtualCollectionRegistryException;

    List<VirtualCollection> getVirtualCollections(int first, int count, QueryFactory qryFactory) throws VirtualCollectionRegistryException;

    List<String> getOrigins() throws VirtualCollectionRegistryException;

    List<User> getUsers();

    User getOrCreateUser(String username) throws VirtualCollectionRegistryException;
    User getOrCreateUser(Principal principal) throws VirtualCollectionRegistryException;

    CreatorService getCreatorService();


    /**
     * Return user object for the given principal
     * @param principal
     * @return
     * @throws VirtualCollectionRegistryException
     */
    User fetchUser(Principal principal) throws VirtualCollectionRegistryException;

    /**
     * Create a new user for the given principal
     * @param principal
     * @return the created User object. Can be null in case of an error.
     * @throws VirtualCollectionRegistryException
     */
    //User createUser(Principal principal)  throws VirtualCollectionRegistryException;

    /**
     * Create a new uer for the given principal if it does not already exist
     * @param principal
     * @return the created or existing User object. Can be null in case of an error.
     * @throws VirtualCollectionRegistryException
     */
    //User createUserIfNotExists(Principal principal)  throws VirtualCollectionRegistryException;

    String getDbVersion() throws VirtualCollectionRegistryException;

    VirtualCollectionRegistryReferenceValidator getReferenceValidator();

} // interface VirtualCollectionRegistry
