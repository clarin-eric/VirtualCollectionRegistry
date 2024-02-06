package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.CreatorValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.KeywordValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.NameValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.QueryValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.ReproducibilityNoticeValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.ResourceValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.TypeValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedByQuery;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.ValidationError;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * A virtual collection validator. Reusable and thread safe.
 *
 * @author twagoo
 */
@Service
@Qualifier("creation")
public class VirtualCollectionValidatorImpl implements VirtualCollectionValidator {

    @Override
    public void validate(VirtualCollection vc)
            throws VirtualCollectionValidationException {
        if (vc == null) {
            throw new NullPointerException("vc == null");
        }

        VirtualCollectionValidationException exception = 
            new VirtualCollectionValidationException();
        
        final Set<Creator> uniqueCreators = new HashSet<>(16);
        final Set<String> uniqueResourceRefs = new HashSet<>(512);

        // proceed to validate ...
        if ((vc.getName() == null) || vc.getName().trim().isEmpty()) {
            exception.addErrorMessage(new NameValidationFailedMessage("collection has an empty name"));
        }

        if(vc.getCreators().isEmpty()) {
            exception.addErrorMessage(new CreatorValidationFailedMessage(0, null, "collection contains no authors. At least one author is required"));
        }
        
        for (Creator creator : vc.getCreators()) {
            if (uniqueCreators.contains(creator)) {
                exception.addErrorMessage(new CreatorValidationFailedMessage(creator.getId(), creator.getPerson(), "collection contains non unique authors"));
            }
            uniqueCreators.add(creator);
        }

        if(vc.getType() == null) {
            exception.addErrorMessage(new TypeValidationFailedMessage("collection has no type"));
        } else {
            switch (vc.getType()) {
                case EXTENSIONAL:
                    if (vc.getResources().isEmpty()) {
                        exception.addErrorMessage(new TypeValidationFailedMessage("extensional collections must contain on or more resources"));
                    }
                    if (vc.getGeneratedBy() != null) {
                        exception.addErrorMessage(new TypeValidationFailedMessage("extensional collections must not contain GeneratedBy"));
                    }
                    final List<Validatable<String>> invalidRefs = getInvalidReferences(vc);
                    if (!invalidRefs.isEmpty()) {
                        for(Validatable<String> invalidRef : invalidRefs) {
                            for(IValidationError err: invalidRef.getErrors()) {    
                                if(err instanceof ValidationError) {
                                    ValidationError validationError = (ValidationError)err;
                                    if(validationError.getMessage() != null) {
                                        exception.addErrorMessage(new ResourceValidationFailedMessage(invalidRef.getValue(), validationError.getMessage()));
                                    }
                                } else if (err != null) {
                                    exception.addErrorMessage(new ResourceValidationFailedMessage(invalidRef.getValue(), err.toString()));
                                }
                            }
                        }
                    }
                    break;
                case INTENSIONAL:
                    final GeneratedBy generatedBy = vc.getGeneratedBy();
                    if (generatedBy == null) {
                        exception.addErrorMessage(new TypeValidationFailedMessage("intensional collections must contains GeneratedBy"));
                    }
                    if (generatedBy.getDescription() == null) {
                        exception.addErrorMessage(new TypeValidationFailedMessage("GeneratedBy has empty description"));
                    }
                    final GeneratedByQuery query = generatedBy.getQuery();
                    if (query != null) {
                        if (query.getProfile() == null) {
                            exception.addErrorMessage(new QueryValidationFailedMessage("profile of GeneratedBy.Query is empty"));
                        }
                        if (query.getValue() == null) {
                            exception.addErrorMessage(new QueryValidationFailedMessage("query of GeneratedBy.Query is empty"));
                        }
                    }
            }
        }
        
        if ((vc.getReproducibilityNotice() != null)
                && (vc.getReproducibility() == null)) {
            exception.addErrorMessage(new ReproducibilityNoticeValidationFailedMessage("reproducibility notice without reproducubility"));
        }

        for (String keyword : vc.getKeywords()) {
            if ((keyword == null) || (keyword.trim().isEmpty())) {
                exception.addErrorMessage(new KeywordValidationFailedMessage("keyword is empty"));
            }
        }

        for (Resource resource : vc.getResources()) {
            String ref = resource.getRef();
            if (ref == null) {
                exception.addErrorMessage(new ResourceValidationFailedMessage(null, "collection contains resource with empty ResourceRef"));
            } else if (uniqueResourceRefs.contains(ref)) {
                exception.addErrorMessage(new ResourceValidationFailedMessage(null, "collection contains non-unique ResourceRefs"));
            }
            uniqueResourceRefs.add(ref);
        }
        
        exception.throwIfNeeded();
    }

    private List<Validatable<String>> getInvalidReferences(VirtualCollection vc) {
        final ReferenceValidator referenceValidator = new ReferenceValidator();
        final List<Validatable<String>> invalidRefs = new ArrayList<>();
        for (Resource resource : vc.getResources()) {            
            final Validatable<String> validatable = new Validatable<>(resource.getRef());
            referenceValidator.validate(validatable);        
            
            if (!validatable.isValid()) {
                invalidRefs.add(validatable);
            }
        }
        return invalidRefs;
    }

} // class VirtualCollectionValidator
