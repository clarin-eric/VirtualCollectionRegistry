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
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * A virtual collection validator performing "soft" validation that should occur
 * before publication.
 *
 * @author twagoo
 */
@Service(value = "publication-soft")
@Qualifier("publication-soft")
public class VirtualCollectionPrePublicationValidator implements VirtualCollectionValidator {
    
    @Override
    public void validate(VirtualCollection vc) throws VirtualCollectionRegistryUsageException {
//        throw new VirtualCollectionRegistryUsageException("Test", Arrays.asList("First warning", "Second warning", "Third warning"));
    }
    
}
