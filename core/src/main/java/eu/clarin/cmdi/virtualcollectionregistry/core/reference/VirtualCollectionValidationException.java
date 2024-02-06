package eu.clarin.cmdi.virtualcollectionregistry.core.reference;

import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.IValidationFailedMessage;
import java.util.ArrayList;
import java.util.List;

public class VirtualCollectionValidationException extends
        VirtualCollectionRegistryException {

    private static final long serialVersionUID = 1L;    
    
    private final  List<IValidationFailedMessage> messages = new ArrayList<>();
            
    public VirtualCollectionValidationException() {
        super("Virtual Collection failed to validate");
    }
    
    public void addErrorMessage(IValidationFailedMessage errorMessage) {
        this.messages.add(errorMessage);
    }
    
    public List<IValidationFailedMessage> getErrorMessages() {
        return this.messages;
    }
    
    public boolean hasErrorMessages() {
        return !this.messages.isEmpty();
    }
    
    //Old: public void addAllErrorsToSession(ApplicationSession session) {
    public void addAllErrorsToSession(Object session) {
        //this.messages.forEach((m) -> {session.error(m);});
        throw new RuntimeException("Not implemented");
    }
    
    public List<String> getAllErrorsAsList() {
        final List<String> errors = new ArrayList<>();
        this.messages.forEach((error) -> {
            errors.add(error.toString());
        });      
        return errors;
    }
    
    public void throwIfNeeded() throws VirtualCollectionValidationException {
        if(this.hasErrorMessages()) {
            throw this;
        }
    }
} // class VirtualCollectionUsageException
