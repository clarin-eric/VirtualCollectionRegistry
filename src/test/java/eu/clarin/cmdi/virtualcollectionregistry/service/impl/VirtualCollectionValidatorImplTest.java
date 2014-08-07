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

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionValidatorImplTest {

    private VirtualCollectionValidator instance;
    private VirtualCollection vc;

    @Before
    public void setUp() {
        vc = new VirtualCollection();
        instance = new VirtualCollectionValidatorImpl();
    }

    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidateExtensionalEmptyResources() throws Exception {
        vc.setName("Name");
        vc.setType(VirtualCollection.Type.EXTENSIONAL);
        instance.validate(vc);
    }

    @Test
    public void testValidateExtensionalLegalResources() throws Exception {
        vc.setName("Name");
        vc.setType(VirtualCollection.Type.EXTENSIONAL);
        vc.getResources().add(new Resource(Resource.Type.METADATA, "http://clarin.eu"));
        vc.getResources().add(new Resource(Resource.Type.METADATA, "hdl:1234/5678"));
        vc.getResources().add(new Resource(Resource.Type.METADATA, "doi:10.1000/182"));
        try {
            instance.validate(vc);
        } catch (VirtualCollectionRegistryException ex) {
            fail("Validation of valid collection failed: " + ex.getMessage());
        }
    }

    @Test(expected = VirtualCollectionRegistryUsageException.class)
    public void testValidateExtensionalIllegalResources() throws Exception {
        vc.setName("Name");
        vc.setType(VirtualCollection.Type.EXTENSIONAL);
        vc.getResources().add(new Resource(Resource.Type.METADATA, "http://clarin.eu"));
        vc.getResources().add(new Resource(Resource.Type.METADATA, "hdl:1234/5678"));
        vc.getResources().add(new Resource(Resource.Type.METADATA, "doi:10.1000/182"));
        vc.getResources().add(new Resource(Resource.Type.METADATA, "some://illegal/url"));
        instance.validate(vc);
    }

}
