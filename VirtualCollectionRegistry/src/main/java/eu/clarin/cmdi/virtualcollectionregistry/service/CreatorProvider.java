package eu.clarin.cmdi.virtualcollectionregistry.service;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import java.security.Principal;

/**
 *
 * @author twagoo
 */
public interface CreatorProvider {
    
    /**
     * 
     * @return a new creator with the user's details filled in according to the
     * implementation's specification
     */
    Creator getCreator(Principal principal);
    
}
