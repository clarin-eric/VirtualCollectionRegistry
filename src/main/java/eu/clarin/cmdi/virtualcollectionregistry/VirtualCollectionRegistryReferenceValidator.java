package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;

import java.io.Serializable;
import java.util.List;

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
