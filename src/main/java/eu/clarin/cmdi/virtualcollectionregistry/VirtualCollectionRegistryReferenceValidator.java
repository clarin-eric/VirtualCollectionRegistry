package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;

import java.util.List;

public interface VirtualCollectionRegistryReferenceValidator {
    void perform(long now);
    void addReferenceValidationJob(String id, Resource r);
    void removeReferenceValidationJob(String id);
    ReferencesEditor.State getState(String id);
    void setState(String id, ReferencesEditor.State state);
    List<VirtualCollectionRegistryReferenceValidationJob> getJobs();
}
