/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.model.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class VirtualCollectionTest {
 
    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionTest.class);
    /*
    @Test
    public void testSerializeVirtualCollection() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        
        Creator c = new Creator("Elbers", "Willem");
        c.setEMail("willem@clarin.eu");
        c.setOrganisation("CLARIN ERIC");
        
        VirtualCollection vc = 
            VirtualCollectionFactory
                .createNew(new User("Willem Elbers", "Willem"), "Test", "This is a test collection")
                .addKeyword("test")
                .addCreator(c)
                .addResource(new Resource(Resource.Type.RESOURCE, "http://www.clarin.eu", "http://www.clarin.eu", "Clarin website as test resource"))
                .getCollection();
        
        
        String json = mapper.writeValueAsString(vc);
        logger.info("Json: {}", json);
    }
*/
}
