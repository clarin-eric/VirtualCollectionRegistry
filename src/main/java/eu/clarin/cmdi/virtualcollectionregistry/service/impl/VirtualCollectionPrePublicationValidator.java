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

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.GenericValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.QueryValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.ResourceValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
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
    public void validate(VirtualCollection vc) throws VirtualCollectionValidationException {
        VirtualCollectionValidationException exception = new VirtualCollectionValidationException();

        switch (vc.getType()) {
            case EXTENSIONAL:
                validateResources(vc, exception);
                break;
            case INTENSIONAL:
                validateGeneratedBy(vc, exception);
                break;
        }

        if (Strings.isEmpty(vc.getDescription())) {
            exception.addErrorMessage(new GenericValidationFailedMessage("The collection has no description"));
        }

        if (vc.getPurpose() == null) {
            exception.addErrorMessage(new GenericValidationFailedMessage("The purpose has not been specified"));
        }

        if (vc.getPurpose() == VirtualCollection.Purpose.FUTURE_USE) {
            exception.addErrorMessage(new GenericValidationFailedMessage("The reproducibility of the collection has been marked 'future use'"));
        }

        if (vc.getReproducibility() == null) {
            exception.addErrorMessage(new GenericValidationFailedMessage("The degree of reproducibility has not been specified"));
        }

        if (vc.getCreators().isEmpty()) {
            exception.addErrorMessage(new GenericValidationFailedMessage("No creators have been specified for the collection"));
        }

        exception.throwIfNeeded();
    }

    private void validateResources(VirtualCollection vc, VirtualCollectionValidationException exception) {
        int nonPidCount = 0;
        for (Resource resource : vc.getResources()) {
            if (!HandleLinkModel.isSupportedPersistentIdentifier(resource.getRef())) {
                exception.addErrorMessage(
                    new ResourceValidationFailedMessage(
                        resource.getRef(), "The resource URI is not a supported persistent identifer"));
                nonPidCount++;
            }
        }
        if (nonPidCount == 0) {
            return;
        }

        if (nonPidCount == 1) {
            exception.addErrorMessage(new ResourceValidationFailedMessage(null, String.format("One resource is not referenced through a persistent identifier", nonPidCount)));
        } else {
            exception.addErrorMessage(new ResourceValidationFailedMessage(null, String.format("%d resources are not referenced through a persistent identifier", nonPidCount)));
        }
    }

    private void validateGeneratedBy(VirtualCollection vc, VirtualCollectionValidationException exception) {
        final String queryUri = vc.getGeneratedBy().getURI();
        if (!HandleLinkModel.isSupportedPersistentIdentifier(queryUri)) {
            exception.addErrorMessage(new QueryValidationFailedMessage("The query URI is not a supported persistent identifer"));
        }
    }

}
