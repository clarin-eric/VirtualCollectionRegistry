package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifier;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import static java.text.DateFormat.SHORT;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionCMDIWriterImplTest extends XMLTestCase {

    private static final String CONTROL_INSTANCE = "/vc-instance1.xml";

    /**
     * Patterns for paths to be ignored in XML comparison
     */
    private final List<Pattern> IGNORE_PATHS = new CopyOnWriteArrayList<Pattern>(new Pattern[]{
        //ignore creation date
        Pattern.compile("\\/CMD\\[1\\]\\/Header\\[1\\]\\/MdCreationDate\\[1\\]\\/text\\(\\).*"),
        //ignore resource proxy id's
        Pattern.compile("\\/CMD\\[1\\]\\/Resources\\[1\\]\\/ResourceProxyList\\[1\\]\\/ResourceProxy\\[.*\\]\\/@id")});

    /**
     * Test of writeCMDI method, of class VirtualCollectionCMDIWriterImpl.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testWriteCMDIExtensional() throws Exception {
        final VirtualCollectionCMDIWriterImpl instance = new VirtualCollectionCMDIWriterImpl(new VirtualCollectionCMDICreatorImpl());

        // create a collection to serialize
        final VirtualCollection vc = createTestVC();

        // prepare an output writer
        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
        final StringWriter stringWriter = new StringWriter();
        final XMLStreamWriter xmlWriter = xmlOutputFactory.createXMLStreamWriter(stringWriter);

        // write CMDI for instance to the writer
        xmlWriter.writeStartDocument();
        instance.writeCMDI(xmlWriter, vc);
        xmlWriter.writeEndDocument();
        xmlWriter.close();

        // compare output to control document
        final StringReader testOutputReader = new StringReader(stringWriter.toString());
        final InputStreamReader controlReader = new InputStreamReader(getClass().getResourceAsStream(CONTROL_INSTANCE));

        assertCMDIEqual(controlReader, testOutputReader);
    }

    private VirtualCollection createTestVC() throws ParseException {
        final VirtualCollection vc = new VirtualCollection();
        vc.setName("Virtual Collection Name");
        vc.setDescription("Test collection description");
        vc.setType(VirtualCollection.Type.EXTENSIONAL);
        vc.setReproducibility(VirtualCollection.Reproducibility.INTENDED);
        vc.setPurpose(VirtualCollection.Purpose.SAMPLE);

        vc.setOwner(new User("Test user"));
        vc.setCreationDate(DateFormat.getDateInstance(SHORT, Locale.US).parse("01/01/14"));

        vc.setState(VirtualCollection.State.PUBLIC_PENDING);
        //setting the pid requires 'public pending' state and will set the state to public
        vc.setPersistentIdentifier(new PersistentIdentifier(vc, PersistentIdentifier.Type.HANDLE, "9999/1234-5678"));

        final Creator creator = new Creator("Test creator");
        creator.setEMail("test@creator.org");
        creator.setOrganisation("Test Inc.");
        vc.getCreators().add(creator);

        vc.getResources().add(new Resource(Resource.Type.METADATA, "http://my/metadata.cmdi"));
        vc.getResources().add(new Resource(Resource.Type.RESOURCE, "http://my/resource.mpg"));
        return vc;
    }

    private void assertCMDIEqual(final InputStreamReader controlReader, final StringReader testOutputReader) throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        final Diff diff = XMLUnit.compareXML(controlReader, testOutputReader);
        setIgnorePaths(diff);

        assertTrue(diff.toString(), diff.similar());
    }

    /**
     * approach based on
     * http://ayazanwar.wordpress.com/2012/09/23/xmlunit-ignoring-certain-pieces-of-xml-during-xml-comparison-using-regular-expression/
     *
     * @param diff
     * @param ignorePaths
     */
    private void setIgnorePaths(final Diff diff) {
        diff.overrideDifferenceListener(new DifferenceListener() {

            @Override
            public int differenceFound(Difference difference) {
                final String testXPath = difference.getTestNodeDetail().getXpathLocation();
                for (Pattern ignorePath : IGNORE_PATHS) {
                    if (ignorePath.matcher(testXPath).find()) {
                        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                    }
                }
                return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
            }

            @Override
            public void skippedComparison(Node control, Node test) {
            }
        });
    }

}
