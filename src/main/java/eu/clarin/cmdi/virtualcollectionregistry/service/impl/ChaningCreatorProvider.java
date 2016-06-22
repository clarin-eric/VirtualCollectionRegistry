package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author twagoo
 */
public class ChaningCreatorProvider implements CreatorProvider {

    private final List<CreatorProvider> providerChain;

    public ChaningCreatorProvider(List<CreatorProvider> providerChain) {
        this.providerChain = providerChain;
    }

    @Override
    public Creator getCreator(Principal principal) {
        if(principal == null) {
            return null;
        }
        
        final List<Creator> creators = new ArrayList<>(providerChain.size());
        for (CreatorProvider provider : providerChain) {
            creators.add(provider.getCreator(principal));
        }

        final Creator creator = new Creator();
        for (Creator template : creators) {
            if (template.getAddress() != null) {
                creator.setAddress(template.getAddress());
                break;
            }
        }
        for (Creator template : creators) {
            if (template.getEMail() != null) {
                creator.setEMail(template.getEMail());
                break;
            }
        }
        for (Creator template : creators) {
            if (template.getOrganisation() != null) {
                creator.setOrganisation(template.getOrganisation());
                break;
            }
        }
        for (Creator template : creators) {
            if (template.getPerson() != null) {
                creator.setPerson(template.getPerson());
                break;
            }
        }
        for (Creator template : creators) {
            if (template.getRole() != null) {
                creator.setRole(template.getRole());
                break;
            }
        }
        for (Creator template : creators) {
            if (template.getTelephone() != null) {
                creator.setTelephone(template.getTelephone());
                break;
            }
        }
        for (Creator template : creators) {
            if (template.getWebsite() != null) {
                creator.setWebsite(template.getWebsite());
                break;
            }
        }
        return creator;
    }

}
