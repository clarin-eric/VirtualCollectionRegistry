package eu.clarin.cmdi.virtualcollectionregistry;

public class VirtualCollectionRegistryUsageException extends
		VirtualCollectionRegistryException {
	private static final long serialVersionUID = 1L;

	public VirtualCollectionRegistryUsageException(String msg) {
		this(msg, null);
	}

	public VirtualCollectionRegistryUsageException(String msg,
			Throwable cause) {
		super(msg, cause);
	}

} // class VirtualCollectionUsageException
