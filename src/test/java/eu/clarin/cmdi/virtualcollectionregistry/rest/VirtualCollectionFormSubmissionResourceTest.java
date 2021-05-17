/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.rest;


import de.mpg.aai.security.auth.model.BasePrincipal;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionBuilder;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author wilelb
 */
public class VirtualCollectionFormSubmissionResourceTest {
    
    private final static long ID = 123L;
    
    private final Mockery context = new JUnit4Mockery();

    private VirtualCollectionFormSubmissionResource instance;
    private VirtualCollectionRegistry registry;

    private UriInfo uriInfo;
    
    private SecurityContext security;
    
    @Before
    public void setUp() throws Exception {
        registry = context.mock(VirtualCollectionRegistry.class);
        uriInfo = context.mock(UriInfo.class);
        security = context.mock(SecurityContext.class);
        instance = new VirtualCollectionFormSubmissionResource(registry, security, uriInfo);
    }
    
    @Test
    public void testCreateVirtualCollection() throws IOException, VirtualCollectionRegistryException {
        final Principal principal = new BasePrincipal("test_user");
        
        VirtualCollection vc2 = 
                new VirtualCollectionBuilder()
                .setName("Test ")
                .setDescription("Test description")
                .addMetadataResource("https://www.clarin.eu/metedata/1", null)
                .addResourceResource("https://www.clarin.eu/resource/1", null)
                .addKeyword("Test 1")
                .addKeyword("Test 2")
                .build();
        
        final VirtualCollection.Type type = VirtualCollection.Type.EXTENSIONAL;
        final String name = "Test VC";
        final List<String> metadataUris = new ArrayList<>();        
        metadataUris.add("https://www.clarin.eu/metedata/1");
        final List<String> resourceUris = new ArrayList<>();
        resourceUris.add("https://www.clarin.eu/resource/1");
        final String description = "Test description";
           
        //optional params
        final List<String> keywords = new ArrayList<>();
        keywords.add("test");
        final VirtualCollection.Purpose purpose = VirtualCollection.Purpose.REFERENCE;
        final VirtualCollection.Reproducibility reproducibility = VirtualCollection.Reproducibility.INTENDED;
        final String reproducibilityNotice = null;
        final Date creationDate = null;
        final String intensionalDescription = null;
        final String intensionalUri = null;
        final String intensionalQueryProfile = null;
        final String intensionalQueryValue = null;       
       
        
        context.checking(new Expectations() {
            {                
                allowing(security).getUserPrincipal();
                will(returnValue(principal));                
                oneOf(uriInfo).getBaseUriBuilder();
                will(returnValue(new JerseyUriBuilder().uri(URI.create("/mycontextpath"))));
                oneOf(registry).createVirtualCollection(with(equal(principal)), with(any(VirtualCollection.class)));
                will(returnValue(ID));
            }
        });

        final Response result = instance.submitNewVc(type, name, metadataUris, 
                resourceUris, description, keywords, purpose, reproducibility, 
                reproducibilityNotice, creationDate, 
                intensionalDescription, intensionalUri, intensionalQueryProfile, intensionalQueryValue);

        assertEquals(303, result.getStatus());
        assertEquals(true, result.getMetadata().containsKey("Location"));
        assertEquals(false, result.getMetadata().get("Location").isEmpty());
        assertEquals(URI.create("/mycontextpath/../edit/123"), result.getMetadata().get("Location").get(0));    
    }
}
