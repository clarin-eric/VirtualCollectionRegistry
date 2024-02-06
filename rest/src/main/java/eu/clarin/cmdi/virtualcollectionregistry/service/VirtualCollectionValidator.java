package eu.clarin.cmdi.virtualcollectionregistry.service;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

/**
 *
 * @author twagoo
 */
public interface VirtualCollectionValidator {

    void validate(VirtualCollection vc) throws VirtualCollectionValidationException;

}
