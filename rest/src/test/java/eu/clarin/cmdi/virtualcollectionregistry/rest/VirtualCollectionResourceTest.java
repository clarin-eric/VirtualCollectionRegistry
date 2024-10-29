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
package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Variant;
import static org.hamcrest.Matchers.*;
import org.jmock.Expectations;
import static org.jmock.Expectations.equal;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionResourceTest {

    private final static long ID = 123L;

    private Mockery context = new JUnit4Mockery();

    private VirtualCollectionResource instance;
    private VirtualCollectionRegistry registry;
    private VirtualCollectionMarshaller marshaller;

    private UriInfo uriInfo;
    private HttpHeaders headers;
    private SecurityContext security;

    @Before
    public void setUp() throws Exception {
        registry = context.mock(VirtualCollectionRegistry.class);
        marshaller = context.mock(VirtualCollectionMarshaller.class);
        uriInfo = context.mock(UriInfo.class);
        headers = context.mock(HttpHeaders.class);
        security = context.mock(SecurityContext.class);
        instance = new VirtualCollectionResource(registry, marshaller, security, headers, uriInfo, ID);
    }

    /**
     * Test of getVirtualCollection method, of class VirtualCollectionResource.
     */
    @Test
    public void testGetPublicVirtualCollection() throws Exception {
        final Request request = context.mock(Request.class);

        // create a public VC to have returned by the registry service
        final VirtualCollection vc = new VirtualCollection();
        vc.setId(ID);
        vc.setName("Test VC");
        vc.setState(VirtualCollection.State.PUBLIC_PENDING);
        // set PID and set state to public
        vc.addPersistentIdentifier(new PersistentIdentifier(vc, PersistentIdentifier.Type.DUMMY, "PID"));

        context.checking(new Expectations() {
            {
                oneOf(registry).retrieveVirtualCollection(ID);
                will(returnValue(vc));
            }
        });
        final Response response = instance.getVirtualCollection(request);
        assertEquals(200, response.getStatus());
        assertEquals(vc, response.getEntity());
        assertEquals(VirtualCollection.State.PUBLIC_PENDING, vc.getState());
    }

    /**
     * Test of getVirtualCollection method, of class VirtualCollectionResource.
     */
    @Test
    public void testGetPublicFrozenVirtualCollection() throws Exception {
        final Request request = context.mock(Request.class);

        // create a public VC to have returned by the registry service
        final VirtualCollection vc = new VirtualCollection();
        vc.setId(ID);
        vc.setName("Test VC");
        vc.setState(VirtualCollection.State.PUBLIC_FROZEN_PENDING);
        // set PID and set state to public_frozen
        vc.addPersistentIdentifier(new PersistentIdentifier(vc, PersistentIdentifier.Type.DUMMY, "PID"));

        context.checking(new Expectations() {
            {
                oneOf(registry).retrieveVirtualCollection(ID);
                will(returnValue(vc));
            }
        });
        final Response response = instance.getVirtualCollection(request);
        assertEquals(200, response.getStatus());
        assertEquals(vc, response.getEntity());
        assertEquals(VirtualCollection.State.PUBLIC_FROZEN_PENDING, vc.getState());
    }
    
    /**
     * Test of getVirtualCollection method, of class VirtualCollectionResource.
     */
    @Test
    public void testGetPrivateVirtualCollection() throws Exception {
        final Request request = context.mock(Request.class);

        // create a public VC to have returned by the registry service
        final VirtualCollection vc = new VirtualCollection();
        vc.setId(ID);
        vc.setName("Test VC");
        vc.setState(VirtualCollection.State.PRIVATE);

        // prepare variants for content negotiation
        final List<Variant> variants = Variant.mediaTypes(MediaType.APPLICATION_XML_TYPE).add().build();

        context.checking(new Expectations() {
            {
                oneOf(registry).retrieveVirtualCollection(ID);
                will(returnValue(vc));
                // it's private, so service will check whether non-CMDI XML is acceptable
                exactly(1).of(equal(request))
                        .method("selectVariant").with(hasItems(variants.toArray()));
                // we will accept XML
                will(returnValue(variants.get(0)));
            }
        });
        final Response response = instance.getVirtualCollection(request);
        assertEquals(200, response.getStatus());
        assertEquals(vc, response.getEntity());
    }

    /**
     * Test of getVirtualCollectionDetailsRedirect method, of class
     * VirtualCollectionResource.
     */
    @Test
    public void testGetVirtualCollectionDetailsRedirect() throws Exception {
        /*
        context.checking(new Expectations() {
            {
                oneOf(uriInfo).getBaseUriBuilder();
                will(returnValue(new UriBuilderImpl().path("http://server/vcr")));
            }
        });
        final Response response = instance.getVirtualCollectionDetailsRedirect();
        assertEquals(303, response.getStatus());
        final List<Object> location = response.getMetadata().get("Location");
        assertFalse(location.isEmpty());
        assertEquals(URI.create("http://server/vcr/../details/" + ID), location.get(0));
        */
    }

    /**
     * Test of updateVirtualCollection method, of class
     * VirtualCollectionResource.
     */
    @Test
    public void testUpdateVirtualCollection() throws Exception {
        // principal and input stream stubs (usually provided by client via Jersey)
        final Principal principal = context.mock(Principal.class);
        final InputStream input = new ByteArrayInputStream(new byte[0]);

        // create a public VC to have returned by the marshaller
        final VirtualCollection vc = new VirtualCollection();
        vc.setId(ID);
        vc.setName("Test VC");
        vc.setState(VirtualCollection.State.PRIVATE);

        context.checking(new Expectations() {
            {
                oneOf(security).getUserPrincipal();
                will(returnValue(principal));
                allowing(headers).getRequestHeaders();
                allowing(headers).getMediaType();
                will(returnValue(MediaType.APPLICATION_XML_TYPE));
                oneOf(marshaller).unmarshal(input, VirtualCollection.Format.XML, "utf-8");
                will(returnValue(vc));
                oneOf(registry).updateVirtualCollection(principal, ID, vc);
                will(returnValue(ID));
            }
        });

        final Response result = instance.updateVirtualCollection(input);
        assertEquals(200, result.getStatus());
    }

    /**
     * Test of deleteVirtualCollection method, of class
     * VirtualCollectionResource.
     */
    @Test
    public void testDeleteVirtualCollection() throws Exception {
        // principal stream stub (usually provided by client via Jersey)
        final Principal principal = context.mock(Principal.class);

        context.checking(new Expectations() {
            {
                oneOf(security).getUserPrincipal();
                will(returnValue(principal));
                oneOf(registry).deleteVirtualCollection(principal, ID);
                will(returnValue(ID));
            }
        });

        final Response result = instance.deleteVirtualCollection();
        assertEquals(200, result.getStatus());
    }

    /**
     * Test of getVirtualCollectionState method, of class
     * VirtualCollectionResource.
     */
    @Test
    public void testGetVirtualCollectionState() throws Exception {
        // private state
        context.checking(new Expectations() {
            {
                oneOf(registry).getVirtualCollectionState(ID);
                will(returnValue(VirtualCollection.State.PRIVATE));
            }
        });
        Response result = instance.getVirtualCollectionState();
        assertEquals(200, result.getStatus());
        assertEquals(State.PRIVATE, result.getEntity());

        // public state
        context.checking(new Expectations() {
            {
                oneOf(registry).getVirtualCollectionState(ID);
                will(returnValue(VirtualCollection.State.PUBLIC));
            }
        });
        result = instance.getVirtualCollectionState();
        assertEquals(200, result.getStatus());
        assertEquals(State.PUBLIC, result.getEntity());
    }

    /**
     * Test of setVirtualCollectionState method, of class
     * VirtualCollectionResource.
     */
    @Test
    public void testSetVirtualCollectionState() throws Exception {
        // principal stream stub (usually provided by client via Jersey)
        final Principal principal = context.mock(Principal.class);

        // set to private
        context.checking(new Expectations() {
            {
                allowing(security).getUserPrincipal();
                will(returnValue(principal));

                oneOf(registry).setVirtualCollectionState(principal, ID, VirtualCollection.State.PRIVATE);
            }
        });
        Response result = instance.setVirtualCollectionState(ID, State.PRIVATE);
        assertEquals(200, result.getStatus());

        // set to public
        context.checking(new Expectations() {
            {
                // setting public status will *mark* it for publication, so the
                // actual status will be 'public pending' initially
                oneOf(registry).setVirtualCollectionState(principal, ID, VirtualCollection.State.PUBLIC_PENDING);
            }
        });
        result = instance.setVirtualCollectionState(ID, State.PUBLIC);
        assertEquals(200, result.getStatus());
    }

}
