package eu.clarin.cmdi.virtualcollectionregistry.core.reference;

import eu.clarin.cmdi.virtualcollectionregistry.core.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistryException;
import java.io.Serializable;


public interface VirtualCollectionRegistryReferenceValidator extends Serializable {
    void perform(long now, DataStore datastore) throws VirtualCollectionRegistryException;
    void shutdown();

    /*
    void addReferenceValidationJob(String sessionId, String id, Resource r);
    void removeReferenceValidationJob(String id);
    ReferencesEditor.State getState(String id);
    void setState(String id, ReferencesEditor.State state);
    List<VirtualCollectionRegistryReferenceValidationJob> getJobs();
    VirtualCollectionRegistryReferenceValidationJob getJob(String id);
     */
}
