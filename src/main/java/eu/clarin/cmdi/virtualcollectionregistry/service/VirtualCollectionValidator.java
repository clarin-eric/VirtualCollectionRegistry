package eu.clarin.cmdi.virtualcollectionregistry.service;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

/**
 *
 * @author twagoo
 */
public interface VirtualCollectionValidator {

    void validate(VirtualCollection vc) throws VirtualCollectionRegistryException;

}
