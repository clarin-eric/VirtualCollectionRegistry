package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.HashSet;
import java.util.Set;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

public class VirtualCollectionValidator {
    private Set<Creator> uniqueCreators = new HashSet<Creator>(16);
    private Set<String> uniqueResourceRefs = new HashSet<String>(512);

    public void validate(VirtualCollection vc)
            throws VirtualCollectionRegistryException {
        if (vc == null) {
            throw new NullPointerException("vc == null");
        }

        // reset internal state
        reset();

        // proceed to validate ...
        if ((vc.getName() == null) || vc.getName().trim().isEmpty()) {
            throw new VirtualCollectionRegistryUsageException(
                    "collection has an empty name");
        }
        
        for (Creator creator : vc.getCreators()) {
            if (uniqueCreators.contains(creator)) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains non unique creators");
            }
            uniqueCreators.add(creator);
        }

        for (Resource resource : vc.getResources()) {
            String ref = resource.getRef();
            if ((ref == null) || ref.trim().isEmpty()) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains resource with empty ResourceRef");
            }
            if (uniqueResourceRefs.contains(ref)) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains non-unique ResourceRefs");
            }
            uniqueResourceRefs.add(ref);
        }
    }

    private void reset() {
        uniqueCreators.clear();
        uniqueResourceRefs.clear();
    }

} // class VirtualCollectionValidator
