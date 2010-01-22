package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.HashMap;

public class VirtualCollectionValidator {
	private HashMap<Integer, Resource> uniqueResources =
		new HashMap<Integer, Resource>();
	private HashMap<String, Resource> uniqueResourceRefs =
		new HashMap<String, Resource>();
	
	public void validate(VirtualCollection vc) {
		for (Resource resource : vc.getResources()) {
			int signature = resource.getSignature();
			if (uniqueResources.containsKey(signature)) {
				throw new IllegalArgumentException("collection contains non-unique resources");
			}
			uniqueResources.put(signature, resource);
			String ref = resource.getRef();
			if ((ref == null) || ref.trim().isEmpty()) {
				throw new IllegalArgumentException("collection contains resource with empty ResourceRef");
			}
			if (uniqueResourceRefs.containsKey(ref)) {
				throw new IllegalArgumentException("collection contains non-unique ResourceRefs");
			}
			uniqueResourceRefs.put(ref, resource);
		}
	}

} // class VirtualCollectionValidator
