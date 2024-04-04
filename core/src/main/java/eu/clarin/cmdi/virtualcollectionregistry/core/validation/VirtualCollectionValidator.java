package eu.clarin.cmdi.virtualcollectionregistry.core.validation;

import eu.clarin.cmdi.virtualcollectionregistry.core.reference.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;

/**
 *
 * @author twagoo
 */
public interface VirtualCollectionValidator {

    void validate(VirtualCollection vc) throws VirtualCollectionValidationException;

}
