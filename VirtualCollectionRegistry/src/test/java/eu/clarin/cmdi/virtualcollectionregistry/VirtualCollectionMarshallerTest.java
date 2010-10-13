package eu.clarin.cmdi.virtualcollectionregistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Test;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionMarshaller;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionMarshaller.Format;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

public class VirtualCollectionMarshallerTest {

    @Test
    public void testMinmalExtensional() throws Exception {
        VirtualCollectionMarshaller m =
           new VirtualCollectionMarshaller();
        VirtualCollection vc =
            new VirtualCollection(VirtualCollection.Type.EXTENSIONAL, "Test 12");
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
        VirtualCollectionMarshaller m =
            new VirtualCollectionMarshaller();
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
        assertEquals("Joe User", c.getName());
        assertEquals("joe.user@example.com", c.getEMail());
        assertEquals("Example Organization", c.getOrganisation());
        c = vc.getCreators().get(1);
        assertNotNull(c);
        assertEquals("Jane User", c.getName());
        assertEquals("jane.user@example.com", c.getEMail());
        assertEquals("Example Organization", c.getOrganisation());

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
        m.unmarshal(new ByteArrayInputStream(out.toByteArray()),
                Format.XML, "utf-8");
    }

    @Test
    public void testUnmarshallIntensionalCollection() throws Exception {
        VirtualCollectionMarshaller m =
            new VirtualCollectionMarshaller();
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
        assertEquals("Joe User", c.getName());
        assertEquals("joe.user@example.com", c.getEMail());
        assertEquals("Example Organization", c.getOrganisation());


        assertEquals(0, vc.getResources().size());

        GeneratedBy generatedBy = vc.getGeneratedBy();
        assertNotNull(generatedBy);
        assertEquals("GeneratedBy description", generatedBy.getDescription());
        assertEquals("urn:x-vcr:test-uri", generatedBy.getURI());
        GeneratedBy.Query query = generatedBy.getQuery();
        assertNotNull(query);
        assertEquals("sql", query.getProfile());
        assertEquals("<SQL>Test-Query</SQL>", query.getValue());

        // round-tripping
        ByteArrayOutputStream out = new ByteArrayOutputStream(65536);
        m.marshal(out, Format.XML, vc);
        m.unmarshal(new ByteArrayInputStream(out.toByteArray()),
                Format.XML, "utf-8");
    }

} // class VirtualCollectionMarshallerTest
