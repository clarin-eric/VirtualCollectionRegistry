package eu.clarin.cmdi.virtualcollectionregistry;

public class VirtualCollectionNotFoundException extends
		VirtualCollectionRegistryException {
	private static final long serialVersionUID = 1L;

	public VirtualCollectionNotFoundException(long id) {
		super("virtual collection with id " + id + " was not found");
	}

	public VirtualCollectionNotFoundException(String uuid) {
		super("virtual collection with UUID " + uuid + " was not found");
	}

} // class VirtualCollectionNotFoundException
