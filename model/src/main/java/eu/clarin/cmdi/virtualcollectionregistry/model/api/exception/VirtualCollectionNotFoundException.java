package eu.clarin.cmdi.virtualcollectionregistry.model.api.exception;

public class VirtualCollectionNotFoundException extends
        VirtualCollectionRegistryException {
    private static final long serialVersionUID = 1L;

    public VirtualCollectionNotFoundException(long id) {
        super("virtual collection with id " + id + " was not found");
    }

} // class VirtualCollectionNotFoundException
