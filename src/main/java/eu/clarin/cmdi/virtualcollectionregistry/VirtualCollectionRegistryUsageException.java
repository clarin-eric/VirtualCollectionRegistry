package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.Collections;
import java.util.List;

public class VirtualCollectionRegistryUsageException extends
        VirtualCollectionRegistryException {

    private static final long serialVersionUID = 1L;

    private final List<String> validationErrors;

    public VirtualCollectionRegistryUsageException(String msg) {
        this(msg, null);
    }

    public VirtualCollectionRegistryUsageException(String msg,
            Throwable cause) {
        this(msg, cause, null);
    }

    public VirtualCollectionRegistryUsageException(String msg,
            Throwable cause, List<String> validationErrors) {
        super(msg, cause);
        this.validationErrors = validationErrors;
    }

    /**
     *
     * @return list of validation errors (never null)
     */
    public List<String> getValidationErrors() {
        if (validationErrors == null) {
            return Collections.emptyList();
        } else {
            return validationErrors;
        }
    }

} // class VirtualCollectionUsageException
