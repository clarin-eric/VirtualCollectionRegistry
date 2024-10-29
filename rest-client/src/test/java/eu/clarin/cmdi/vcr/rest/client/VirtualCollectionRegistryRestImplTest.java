/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.vcr.rest.client;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.service.impl.VirtualCollectionMarshallerImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class VirtualCollectionRegistryRestImplTest {
    private final Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryRestImplTest.class);
    
    private final VirtualCollectionRegistryRestImpl impl;

    public VirtualCollectionRegistryRestImplTest() throws VirtualCollectionRegistryException {
        this.impl = new VirtualCollectionRegistryRestImpl(new VirtualCollectionMarshallerImpl());
    }
    
    @Test
    public void testGetAllCollections() throws VirtualCollectionRegistryException {
        //VirtualCollectionList list = impl.getVirtualCollections(null, 0, 0); 
        //logger.info("Number of collections: {}", list.getTotalCount());
    }
}
