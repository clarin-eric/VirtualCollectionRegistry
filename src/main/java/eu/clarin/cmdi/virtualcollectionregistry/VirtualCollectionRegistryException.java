package eu.clarin.cmdi.virtualcollectionregistry;

public class VirtualCollectionRegistryException extends Exception {
	private static final long serialVersionUID = 1L;

	public VirtualCollectionRegistryException(String msg) {
		super(msg);
	}
	
	public VirtualCollectionRegistryException(String msg, Throwable cause) {
		super(msg, cause);
	}

} // class VirtualCollectionRegistryException
