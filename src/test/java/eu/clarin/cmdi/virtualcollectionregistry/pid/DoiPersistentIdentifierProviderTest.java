package eu.clarin.cmdi.virtualcollectionregistry.pid;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.clarin.cmdi.virtualcollectionregistry.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.PermaLinkServiceImpl;
import eu.clarin.cmdi.virtualcollectionregistry.TestApplication;
import eu.clarin.cmdi.virtualcollectionregistry.WicketTesterEnabledTest;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class DoiPersistentIdentifierProviderTest extends WicketTesterEnabledTest {
    private static final Logger logger = LoggerFactory.getLogger(DoiPersistentIdentifierProvider.class);

    private static String prefix = "10.5438";

    private PermaLinkService permaLinkService = new PermaLinkServiceImpl(TestApplication.BASE_URI);

    @Test
    public void testCreate() throws JsonProcessingException, UnsupportedEncodingException {

        VirtualCollection vc = new VirtualCollection();
        vc.setId(1L);
        vc.setName("Test VC éë");
        Creator c = new Creator("Test", " Person");
        c.setOrganisation("Test Org");
        vc.getCreators().add(c);
        vc.setCreationDate(new Date());

        DoiRequest request = DoiRequestBuilder.createAutoGeneratedDoiRequest(prefix, vc, permaLinkService);
        String json = request.toJsonString();;

        logger.info("Json={}", json);

        StringEntity jsonEntity = new StringEntity(json, StandardCharsets.UTF_8);
        logger.info("entity content type={}", jsonEntity.getContentType());


    }
}
