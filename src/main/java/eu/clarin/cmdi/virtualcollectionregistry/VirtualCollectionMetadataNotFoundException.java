package eu.clarin.cmdi.virtualcollectionregistry;

public class VirtualCollectionMetadataNotFoundException extends
		VirtualCollectionRegistryException {
	private static final long serialVersionUID = 1L;

	public VirtualCollectionMetadataNotFoundException(long id) {
		super("metadata resource with id " + id + " was not found");
	}

} // class VirtualCollectionMetadataNotFoundException
