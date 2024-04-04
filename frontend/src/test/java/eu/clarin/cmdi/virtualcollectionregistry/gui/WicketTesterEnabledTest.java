package eu.clarin.cmdi.virtualcollectionregistry.gui;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;

//@ContextConfiguration(locations = {"/applicationContextTest.xml"})
//@RunWith(SpringJUnit4ClassRunner.class)
public class WicketTesterEnabledTest {
    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(new TestApplication());
    }
}
