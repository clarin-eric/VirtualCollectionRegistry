package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedByQuery;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.wicket.validation.Validatable;
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
            throws VirtualCollectionRegistryUsageException {
        if (vc == null) {
            throw new NullPointerException("vc == null");
        }

        final Set<Creator> uniqueCreators = new HashSet<Creator>(16);
        final Set<String> uniqueResourceRefs = new HashSet<String>(512);

        // proceed to validate ...
        if ((vc.getName() == null) || vc.getName().trim().isEmpty()) {
            throw new VirtualCollectionRegistryUsageException(
                    "collection has an empty name");
        }

        for (Creator creator : vc.getCreators()) {
            if (uniqueCreators.contains(creator)) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains non unique creators");
            }
            uniqueCreators.add(creator);
        }

        if(vc.getType() == null) {
            throw new VirtualCollectionRegistryUsageException("collection has no type");
        } else {
            switch (vc.getType()) {
                case EXTENSIONAL:
                    if (vc.getResources().isEmpty()) {
                        throw new VirtualCollectionRegistryUsageException(
                                "extensional collection must contain on or "
                                + "more resources");
                    }
                    if (vc.getGeneratedBy() != null) {
                        throw new VirtualCollectionRegistryUsageException(
                                "extensional collection must not contain GeneratedBy");
                    }
                    final List<Validatable<String>> invalidRefs = getInvalidReferences(vc);
                    if (!invalidRefs.isEmpty()) {
                        throw new VirtualCollectionRegistryUsageException("One or more references are not valid", invalidRefs, null);
                                /*String.format(
                                        "one or more references are not valid: %s",
                                        invalidRefs));*/
                    }
                    break;
                case INTENSIONAL:
                    final GeneratedBy generatedBy = vc.getGeneratedBy();
                    if (generatedBy == null) {
                        throw new VirtualCollectionRegistryUsageException(
                                "intensional collections needs GeneratedBy");
                    }
                    if (generatedBy.getDescription() == null) {
                        throw new VirtualCollectionRegistryUsageException(
                                "GeneratedBy has empty description");
                    }
                    final GeneratedByQuery query = generatedBy.getQuery();
                    if (query != null) {
                        if (query.getProfile() == null) {
                            throw new VirtualCollectionRegistryUsageException(
                                    "profile of GeneratedBy.Query is empty");
                        }
                        if (query.getValue() == null) {
                            throw new VirtualCollectionRegistryUsageException(
                                    "query of GeneratedBy.Query is empty");
                        }
                    }
            }
        }
        
        if ((vc.getReproducibilityNotice() != null)
                && (vc.getReproducibility() == null)) {
            throw new VirtualCollectionRegistryUsageException(
                    "reproducibility notice without reproducubility");
        }

        for (String keyword : vc.getKeywords()) {
            if ((keyword == null) || (keyword.trim().isEmpty())) {
                throw new VirtualCollectionRegistryUsageException(
                        "keyword is empty");
            }
        }

        for (Resource resource : vc.getResources()) {
            String ref = resource.getRef();
            if (ref == null) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains resource with empty ResourceRef");
            }
            if (uniqueResourceRefs.contains(ref)) {
                throw new VirtualCollectionRegistryUsageException(
                        "collection contains non-unique ResourceRefs");
            }
            uniqueResourceRefs.add(ref);
        }
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
