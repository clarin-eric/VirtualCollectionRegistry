package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.AuthPrincipal;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.security.Principal;

/**
 *
 * @author twagoo
 */
public class SAMLCreatorProvider implements CreatorProvider {

    @Override
    public Creator getCreator(Principal userPrincipal) {
        final Creator creator = new Creator();        
        if (userPrincipal instanceof AuthPrincipal) {
            final AuthPrincipal principal = (AuthPrincipal) userPrincipal;
            creator.setPerson(getAttribute(principal, "cn")); //TODO: configure more properties
        } else {
            creator.setPerson(userPrincipal.getName());
        }

        return creator;
    }

    private static String getAttribute(final AuthPrincipal principal, String attr) {
//        logger.trace("Looking for attribute {}", attr);
        final AuthAttribute<?> attribute = principal.getAttribues().get(attr);
        if (attribute != null) {
            final Object value = attribute.getValue();
            if (value != null) {
//                logger.trace("Found attribute value: {} = {}", attr, value);
                return value.toString();
            }
        }
        return null;
    }

}
