/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.util.string.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * A virtual collection validator performing "soft" validation that should occur
 * before publication. Validated collections are <em>assumed</em> to be valid
 * according to the 'minimal' validator applied on creation and update of the
 * collection.
 *
 * @author twagoo
 */
@Service(value = "publication-soft")
@Qualifier("publication-soft")
public class VirtualCollectionPrePublicationValidator implements VirtualCollectionValidator {

    @Override
    public void validate(VirtualCollection vc) throws VirtualCollectionRegistryUsageException {
        final List<String> warnings = new ArrayList<>();

        switch (vc.getType()) {
            case EXTENSIONAL:
                validateResources(vc, warnings);
                break;
            case INTENSIONAL:
                validateGeneratedBy(vc, warnings);
                break;
        }

        if (Strings.isEmpty(vc.getDescription())) {
            warnings.add("The collection has no description");
        }

        if (vc.getPurpose() == null) {
            warnings.add("The purpose has not been specified");
        }

        if (vc.getPurpose() == VirtualCollection.Purpose.FUTURE_USE) {
            warnings.add("The reproducibility of the collection has been marked 'future use'");
        }

        if (vc.getReproducibility() == null) {
            warnings.add("The degree of reproducibility has not been specified");
        }

        if (vc.getCreators().isEmpty()) {
            warnings.add("No creators have been specified for the collection");
        }

        if (!warnings.isEmpty()) {
            throw new VirtualCollectionRegistryUsageException("Collection is not fit for publication", warnings);
        }
    }

    private void validateResources(VirtualCollection vc, List<String> warnings) {
        int nonPidCount = 0;
        for (Resource resource : vc.getResources()) {
            if (!HandleLinkModel.isSupportedPersistentIdentifier(resource.getRef())) {
                warnings.add("The resource URI is not a supported persistent identifer");
                nonPidCount++;
            }
        }
        if (nonPidCount == 0) {
            return;
        }

        if (nonPidCount == 1) {
            warnings.add(String.format("One resource is not referenced through a persistent identifier", nonPidCount));
        } else {
            warnings.add(String.format("%d resources are not referenced through a persistent identifier", nonPidCount));
        }
    }

    private void validateGeneratedBy(VirtualCollection vc, List<String> warnings) {
        final String queryUri = vc.getGeneratedBy().getURI();
        if (!HandleLinkModel.isSupportedPersistentIdentifier(queryUri)) {
            warnings.add("The query URI is not a supported persistent identifer");
        }
    }

}
