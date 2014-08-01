package eu.clarin.cmdi.virtualcollectionregistry.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.HandleField;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.interfaces.PidWriter;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifier.Type;
import static org.hamcrest.Matchers.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class EPICPersistentIdentifierProviderTest {

    private final Mockery context = new JUnit4Mockery();
    private final Configuration pidConfig = new Configuration("http://epic/server", "9999", "user", "password");
    private EPICPersistentIdentifierProvider instance;
    private PidWriter pidWriter;

    @Before
    public void setUp() {
        pidWriter = context.mock(PidWriter.class);
        instance = new EPICPersistentIdentifierProvider(pidWriter, pidConfig);
        instance.setBaseUri("http://vcr/");
    }

    /**
     * Test of createIdentifier method, of class
     * EPIC2PersistentIdentifierProvider.
     */
    @Test
    public void testCreateIdentifier() throws Exception {
        VirtualCollection vc = new VirtualCollection();
        vc.setName("VC Name");
        vc.setId(123L);
        vc.getCreators().add(new Creator("Joe Unit"));
        vc.getCreators().add(new Creator("Joe Mock"));

        context.checking(new Expectations() {
            {
                // should call pidwriter
                exactly(1).of(equal(pidWriter)).method("registerNewPID").with(
                        equal(pidConfig),
                        allOf(
                                hasEntry(HandleField.URL, "http://vcr/service/virtualcollections/123"),
                                hasEntry(HandleField.TITLE, "VC Name"),
                                hasEntry(HandleField.CREATOR, "Joe Unit")
                        ),
                        equalTo("VCR-00123")
                );
                will(returnValue("9999/VCR-00123"));
            }
        });

        PersistentIdentifier result = instance.createIdentifier(vc);
        assertEquals("9999/VCR-123", result.getIdentifier());
        assertEquals(Type.HANDLE, result.getType());
    }

}
