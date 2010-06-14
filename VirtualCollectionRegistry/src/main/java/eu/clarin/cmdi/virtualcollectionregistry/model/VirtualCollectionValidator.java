package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.HashMap;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;

public class VirtualCollectionValidator {
    private HashMap<Integer, Resource> uniqueResources =
        new HashMap<Integer, Resource>();
    private HashMap<String, Resource> uniqueResourceRefs =
        new HashMap<String, Resource>();

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
        for (Resource resource : vc.getResources()) {
            int signature = resource.getSignature();
            if (uniqueResources.containsKey(signature)) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains non-unique resources");
            }
            uniqueResources.put(signature, resource);
            String ref = resource.getRef();
            if ((ref == null) || ref.trim().isEmpty()) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains resource with empty ResourceRef");
            }
            if (uniqueResourceRefs.containsKey(ref)) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains non-unique ResourceRefs");
            }
            uniqueResourceRefs.put(ref, resource);
        }
    }

    private void reset() {
        uniqueResources.clear();
        uniqueResourceRefs.clear();
    }

} // class VirtualCollectionValidator
