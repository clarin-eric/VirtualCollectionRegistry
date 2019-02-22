package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.feedback.IValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
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
    
    public void addAllErrorsToSession(ApplicationSession session) {
        this.messages.forEach((m) -> {session.error(m);});
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
