package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.List;

public interface VirtualCollectionRegistryReferenceValidator {
    void perform(long now);
    void addReferenceValidationJob(VirtualCollectionRegistryReferenceValidationJob job);
    void removeReferenceValidationJob(VirtualCollectionRegistryReferenceValidationJob job);
    List<VirtualCollectionRegistryReferenceValidationJob> getJobs();
}
