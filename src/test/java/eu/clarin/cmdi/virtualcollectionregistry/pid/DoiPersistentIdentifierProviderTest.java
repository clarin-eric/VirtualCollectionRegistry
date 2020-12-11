package eu.clarin.cmdi.virtualcollectionregistry.pid;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DoiPersistentIdentifierProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(DoiPersistentIdentifierProvider.class);

    private static String prefix = "10.5438";

    @Test
    public void testCreate() throws JsonProcessingException {

        VirtualCollection vc = new VirtualCollection();
        vc.setId(1L);
        vc.setName("Test VC");
        Creator c = new Creator("Test", " Person");
        c.setOrganisation("Test Org");
        vc.getCreators().add(c);
        vc.setCreationDate(new Date());

        DoiRequestBuilder builder = new DoiRequestBuilder();
        DoiRequest request = builder.createGenerateDoiRequest(prefix, vc, "https://base.vcr.com/");
        String json = request.toJsonString();;

        logger.info("Json={}", json);
    }
}
