package eu.clarin.cmdi.virtualcollectionregistry.model.api.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.validation.Validatable;

public class VirtualCollectionRegistryUsageException extends
        VirtualCollectionRegistryException {

    private static final long serialVersionUID = 1L;

    private final List<Validatable<String>> validationErrors;
    private final List<String> errors;
    
    public VirtualCollectionRegistryUsageException(String msg) {
        this(msg, null, null);
    }

    public VirtualCollectionRegistryUsageException(String msg,
            Throwable cause) {
        this(msg, cause, null, null);
    }

    public VirtualCollectionRegistryUsageException(String msg,
            List<Validatable<String>> validationErrors, List<String> errors) {
        this(msg, null, validationErrors, errors);
    }
     
    public VirtualCollectionRegistryUsageException(String msg,
            Throwable cause, List<Validatable<String>> validationErrors, List<String> errors) {
        super(msg, cause);
        this.validationErrors = validationErrors;
        this.errors = errors;
    }

    /**
     *
     * @return list of validation errors (never null)
     */
    public ArrayList<Validatable<String>> getValidationErrors() {
        if (validationErrors == null) {
            return new ArrayList<>();
        } else {
            ArrayList<Validatable<String>> list = new ArrayList<>(validationErrors.size());
            list.addAll(validationErrors);
            return list;
        }
    }
    
    public List<String> getErrors() {
        if (this.errors == null) {
            return Collections.emptyList();
        }
        return this.errors;
    }

} // class VirtualCollectionUsageException
