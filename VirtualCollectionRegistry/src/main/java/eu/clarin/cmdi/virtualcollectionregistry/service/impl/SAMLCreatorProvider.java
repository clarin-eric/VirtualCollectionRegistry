package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.AuthPrincipal;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class SAMLCreatorProvider implements CreatorProvider {

    private final static Logger logger = LoggerFactory.getLogger(SAMLCreatorProvider.class);
    public static final String[] DISPLAY_NAME_ATTRIBUTE = new String[]{"cn", "commonName", "displayName"};
    public static final String[] ORGANISATION_ATTRIBUTE = new String[]{"o", "organizationName", "schacHomeOrganization"};
    public static final String[] MAIL_ATTRIBUTE = new String[]{"mail"};

    @Override
    public Creator getCreator(Principal userPrincipal) {
        final Creator creator = new Creator();
        if (userPrincipal instanceof AuthPrincipal) {
            final AuthPrincipal principal = (AuthPrincipal) userPrincipal;
            creator.setPerson(getAttribute(principal, DISPLAY_NAME_ATTRIBUTE));
            creator.setOrganisation(getAttribute(principal, ORGANISATION_ATTRIBUTE));
            creator.setEMail(getAttribute(principal, MAIL_ATTRIBUTE));
        }

        if (creator.getPerson() == null) {
            creator.setPerson(userPrincipal.getName());
        }

        return creator;
    }

    private static String getAttribute(final AuthPrincipal principal, String[] attrs) {
        for (String attr : attrs) {
            final String value = getAttributeValue(principal, attr);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String getAttributeValue(final AuthPrincipal principal, String attr) {
        logger.trace("Looking for attribute {}", attr);
        final AuthAttribute<?> attribute = principal.getAttribues().get(attr); 
       if (attribute != null) {
            final Object value = attribute.getValue();
            if (value != null) {
                logger.trace("Found attribute value: {} = {}", attr, value);
                return value.toString();
            }
        }
        return null;
    }

}
