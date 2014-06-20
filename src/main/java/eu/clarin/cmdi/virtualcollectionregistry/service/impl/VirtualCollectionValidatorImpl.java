package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedByQuery;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.HashSet;
import java.util.Set;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * A virtual collection validator. Scoped prototype, because it carries state.
 * Use a factory (e.g. dynamically generated using
 * {@link ServiceLocatorFactoryBean}) to get new instances within a singleton.
 *
 * @author twagoo
 */
@Service
@Scope(value = SCOPE_PROTOTYPE)
public class VirtualCollectionValidatorImpl implements VirtualCollectionValidator {
    private final Set<Creator> uniqueCreators = new HashSet<Creator>(16);
    private final Set<String> uniqueResourceRefs = new HashSet<String>(512);

    @Override
    public void validate(VirtualCollection vc)
            throws VirtualCollectionRegistryException {
        if (vc == null) {
            throw new NullPointerException("vc == null");
        }

        // reset internal state
        reset();

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

        switch (vc.getType()) {
            case EXTENSIONAL:
                if (vc.getResources().isEmpty()) {
                    throw new VirtualCollectionRegistryUsageException(
                        "extensional collection must contain on or " +
                        "more resources");
                }
                if (vc.getGeneratedBy() != null) {
                    throw new VirtualCollectionRegistryUsageException(
                            "extensional collection must not contain GeneratedBy");
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

        if ((vc.getReproducibilityNotice() != null) && 
                (vc.getReproducibility() == null)) {
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

    private void reset() {
        uniqueCreators.clear();
        uniqueResourceRefs.clear();
    }

} // class VirtualCollectionValidator
