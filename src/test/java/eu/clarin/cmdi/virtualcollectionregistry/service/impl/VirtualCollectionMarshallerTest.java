package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Test;

import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller.Format;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedByQuery;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

public class VirtualCollectionMarshallerTest {

    @Test
    public void testMinmalExtensional() throws Exception {
        VirtualCollectionMarshallerImpl m =
           new VirtualCollectionMarshallerImpl();
        VirtualCollection vc = new VirtualCollection();
        vc.setType(VirtualCollection.Type.EXTENSIONAL);
        vc.setName("Test 12");
        vc.getResources().add(new Resource(Resource.Type.RESOURCE, "a/ref/"));

        ByteArrayOutputStream out = new ByteArrayOutputStream(65536);
        m.marshal(out, Format.XML, vc);
        VirtualCollection vc2 =
            m.unmarshal(new ByteArrayInputStream(out.toByteArray()),
                        Format.XML, "utf-8");

        assertEquals(vc, vc2);
    }

    @Test
    public void testUnmarshallExtensionalCollection() throws Exception {
        VirtualCollectionMarshallerImpl m =
            new VirtualCollectionMarshallerImpl();
         InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("VirtualCollection-extensional.xml");
        VirtualCollection vc = m.unmarshal(in, Format.XML, "utf-8");

        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertNotNull(vc);
        assertEquals("Test Collection 1", vc.getName());
        assertEquals(VirtualCollection.Type.EXTENSIONAL, vc.getType());
        assertEquals("Test Description", vc.getDescription());
        assertEquals(sdf.parse("2009-12-31"), vc.getCreationDate());
        assertEquals(VirtualCollection.Purpose.SAMPLE, vc.getPurpose());
        assertEquals(VirtualCollection.Reproducibility.UNTENDED,
                vc.getReproducibility());
        assertEquals("Unmaintaned test collection.",
                vc.getReproducibilityNotice());

        assertNotNull(vc.getKeywords());
        assertEquals(2, vc.getKeywords().size());
        assertNotNull(vc.getKeywords());
        assertEquals("test", vc.getKeywords().get(0));
        assertEquals("unmaintained", vc.getKeywords().get(1));

        assertNotNull(vc.getCreators());
        assertEquals(2, vc.getCreators().size());
        Creator c = vc.getCreators().get(0);
        assertNotNull(c);
        assertEquals("Joe User", c.getPerson());
        assertEquals("Example Address Joe", c.getAddress());
        assertEquals("joe.user@example.com", c.getEMail());
        assertEquals("Example Organization", c.getOrganisation());
        assertEquals("+1-800-555-0101", c.getTelephone());
        assertEquals("http://www.example.org/~joe/", c.getWebsite());
        assertEquals("Test Role Joe", c.getRole());
        c = vc.getCreators().get(1);
        assertNotNull(c);
        assertEquals("Jane User", c.getPerson());
        assertEquals("Example Address Jane", c.getAddress());
        assertEquals("jane.user@example.com", c.getEMail());
        assertEquals("Example Organization", c.getOrganisation());
        assertEquals("+1-800-555-0102", c.getTelephone());
        assertEquals("http://www.example.org/~jane/", c.getWebsite());
        assertEquals("Test Role Jane", c.getRole());

        assertNotNull(vc.getResources());
        assertEquals(3, vc.getResources().size());

        Resource r = vc.getResources().get(0);
        assertNotNull(r);
        assertEquals(Resource.Type.METADATA, r.getType());
        assertEquals("http://hdl.handle.net/1839/00-0000-0000-0005-671C-C",
                r.getRef());
        r = vc.getResources().get(1);
        assertNotNull(r);
        assertEquals(Resource.Type.METADATA, r.getType());
        assertEquals("http://hdl.handle.net/1839/00-0000-0000-0006-671C-C",
                r.getRef());
        r = vc.getResources().get(2);
        assertNotNull(r);
        assertEquals(Resource.Type.METADATA, r.getType());
        assertEquals("http://hdl.handle.net/1839/00-0000-0000-0007-671C-C",
                r.getRef());

        assertNull(vc.getGeneratedBy());

        // round-tripping
        ByteArrayOutputStream out = new ByteArrayOutputStream(65536);
        m.marshal(out, Format.XML, vc);
        VirtualCollection vc2 =
            m.unmarshal(new ByteArrayInputStream(out.toByteArray()),
                    Format.XML, "utf-8");
        assertEquals(vc.getOwner(), vc2.getOwner());
        assertEquals(vc.getPersistentIdentifier(),
                vc2.getPersistentIdentifier());
        assertEquals(vc.getState(), vc2.getState());
        assertEquals(vc.getType(), vc2.getType());
        assertEquals(vc.getName(), vc2.getName());
        assertEquals(vc.getDescription(), vc2.getDescription());
        assertEquals(vc.getCreationDate(), vc2.getCreationDate());
        assertEquals(vc.getCreators(), vc2.getCreators());
        assertEquals(vc.getPurpose(), vc2.getPurpose());
        assertEquals(vc.getReproducibility(), vc2.getReproducibility());
        assertEquals(vc.getReproducibilityNotice(),
                vc2.getReproducibilityNotice());
        assertEquals(vc.getKeywords(), vc2.getKeywords());
        assertEquals(vc.getResources(), vc2.getResources());
        assertEquals(vc.getGeneratedBy(), vc2.getGeneratedBy());
        assertEquals(vc, vc2);
    }

    @Test
    public void testUnmarshallIntensionalCollection() throws Exception {
        VirtualCollectionMarshallerImpl m =
            new VirtualCollectionMarshallerImpl();
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("VirtualCollection-intensional.xml");
        VirtualCollection vc = m.unmarshal(in, Format.XML, "utf-8");

        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertNotNull(vc);
        assertEquals("Test Collection 2", vc.getName());
        assertEquals(VirtualCollection.Type.INTENSIONAL, vc.getType());
        assertEquals("Test Description", vc.getDescription());
        assertEquals(sdf.parse("2009-12-31"), vc.getCreationDate());
        assertEquals(VirtualCollection.Purpose.SAMPLE, vc.getPurpose());
        assertEquals(VirtualCollection.Reproducibility.UNTENDED,
                vc.getReproducibility());
        assertEquals("Unmaintaned test collection.",
                vc.getReproducibilityNotice());

        assertNotNull(vc.getKeywords());
        assertEquals(2, vc.getKeywords().size());
        assertNotNull(vc.getKeywords());
        assertEquals("test", vc.getKeywords().get(0));
        assertEquals("unmaintained", vc.getKeywords().get(1));

        assertNotNull(vc.getCreators());
        assertEquals(1, vc.getCreators().size());
        Creator c = vc.getCreators().get(0);
        assertNotNull(c);
        assertEquals("Joe User", c.getPerson());
        assertEquals("joe.user@example.com", c.getEMail());
        assertEquals("Example Organization", c.getOrganisation());


        assertEquals(0, vc.getResources().size());

        GeneratedBy generatedBy = vc.getGeneratedBy();
        assertNotNull(generatedBy);
        assertEquals("GeneratedBy description", generatedBy.getDescription());
        assertEquals("urn:x-vcr:test-uri", generatedBy.getURI());
        GeneratedByQuery query = generatedBy.getQuery();
        assertNotNull(query);
        assertEquals("sql", query.getProfile());
        assertEquals("<SQL>Test-Query</SQL>", query.getValue());

        // round-tripping
        ByteArrayOutputStream out = new ByteArrayOutputStream(65536);
        m.marshal(out, Format.XML, vc);
        VirtualCollection vc2 =
            m.unmarshal(new ByteArrayInputStream(out.toByteArray()),
                    Format.XML, "utf-8");
        assertEquals(vc.getOwner(), vc2.getOwner());
        assertEquals(vc.getPersistentIdentifier(),
                vc2.getPersistentIdentifier());
        assertEquals(vc.getState(), vc2.getState());
        assertEquals(vc.getType(), vc2.getType());
        assertEquals(vc.getName(), vc2.getName());
        assertEquals(vc.getDescription(), vc2.getDescription());
        assertEquals(vc.getCreationDate(), vc2.getCreationDate());
        assertEquals(vc.getCreators(), vc2.getCreators());
        assertEquals(vc.getPurpose(), vc2.getPurpose());
        assertEquals(vc.getReproducibility(), vc2.getReproducibility());
        assertEquals(vc.getReproducibilityNotice(),
                vc2.getReproducibilityNotice());
        assertEquals(vc.getKeywords(), vc2.getKeywords());
        assertEquals(vc.getResources(), vc2.getResources());
        assertEquals(vc.getGeneratedBy(), vc2.getGeneratedBy());
        assertEquals(vc, vc2);
    }

} // class VirtualCollectionMarshallerTest
