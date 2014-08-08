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
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionPrePublicationValidatorTest {

    private VirtualCollection vc;
    private VirtualCollectionValidator instance;

    @Before
    public void setUp() {
        // start with a extensional collection that is valid (enough)
        vc = new VirtualCollection();
        vc.setType(VirtualCollection.Type.EXTENSIONAL);
        vc.setName("Name");
        vc.setDescription("Description");
        vc.setPurpose(VirtualCollection.Purpose.SAMPLE);
        vc.setReproducibility(VirtualCollection.Reproducibility.INTENDED);
        vc.getCreators().add(new Creator("creator"));
        instance = new VirtualCollectionPrePublicationValidator();
    }
    
    @Test
    public void testValidateResources() {
        // this is a valid resource set
        vc.getResources().add(new Resource(Resource.Type.METADATA, "hdl:1234/5678"));
        vc.getResources().add(new Resource(Resource.Type.METADATA, "doi:1.2.3/456"));
        try {
            instance.validate(vc);
        } catch (VirtualCollectionRegistryUsageException ex) {
            fail("Validation should not fail");
        }
    }
    
    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidateResourcesNonPid() throws Exception {
        // Non-PIDs should not be allowed as references
        vc.getResources().add(new Resource(Resource.Type.METADATA, "hdl:1234/5678"));
        vc.getResources().add(new Resource(Resource.Type.METADATA, "doi:1.2.3/456"));
        vc.getResources().add(new Resource(Resource.Type.METADATA, "http://clarin.eu"));
        instance.validate(vc);
    }
    
    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidateQueryUriNonPid() throws Exception {
        // Non-PIDs should not be allowed as query URI
        vc.setType(VirtualCollection.Type.INTENSIONAL);
        final GeneratedBy generatedBy = new GeneratedBy();
        generatedBy.setURI("http://catalog.clarin.eu/vlo");
        vc.setGeneratedBy(generatedBy);
        instance.validate(vc);
    }
    
    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidateNoDescription() throws Exception {
        // Description should not be allowed to be null
        vc.setDescription(null);
        instance.validate(vc);
    }
    
    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidateNoCreators() throws Exception {
        // Description should not be allowed to be null
        vc.getCreators().clear();
        instance.validate(vc);
    }
    
    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidatePurposeFutureUse() throws Exception {
        // Description should not be allowed to be null
        vc.setPurpose(VirtualCollection.Purpose.FUTURE_USE);
        instance.validate(vc);
    }
    
    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidateNoPurpose() throws Exception {
        // Description should not be allowed to be null
        vc.setPurpose(null);
        instance.validate(vc);
    }
    
    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidateNoReproducibility() throws Exception {
        // Description should not be allowed to be null
        vc.setReproducibility(null);
        instance.validate(vc);
    }
}
