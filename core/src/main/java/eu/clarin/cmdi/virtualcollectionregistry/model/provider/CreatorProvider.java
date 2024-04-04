package eu.clarin.cmdi.virtualcollectionregistry.service;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import java.security.Principal;

/**
 *
 * @author twagoo
 */
public interface CreatorProvider {
    
    /**
     * 
     * @param principal
     * @return a new creator with the user's details filled in according to the
     * implementation's specification
     */
    Creator getCreator(Principal principal);
    
}
