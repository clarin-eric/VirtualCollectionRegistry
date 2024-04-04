package eu.clarin.cmdi.virtualcollectionregistry.model.api.exception;

public class VirtualCollectionRegistryException extends Exception {
    private static final long serialVersionUID = 1L;

    public VirtualCollectionRegistryException(String msg) {
        this(msg, null);
    }

    public VirtualCollectionRegistryException(String msg, Throwable cause) {
        super(msg, cause);
    }

} // class VirtualCollectionRegistryException
