package eu.clarin.cmdi.mscr.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.mscr.client.lib.MscrApiConfiguration;
import eu.clarin.cmdi.mscr.client.lib.MscrApiException;
import eu.clarin.cmdi.mscr.client.lib.MscrClient;
import eu.clarin.cmdi.mscr.client.lib.MscrCrosswalk;
import eu.clarin.cmdi.mscr.client.lib.MscrCrosswalkUploadRequest;
import eu.clarin.cmdi.mscr.client.lib.MscrSchema;
import eu.clarin.cmdi.mscr.client.lib.SearchBuilder;
import eu.clarin.cmdi.mscr.client.lib.impl.MscrClientImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;

/**
 *  OpenAPI documentation:
 *  - https://mscr-test.2.rahtiapp.fi/datamodel-api/swagger-ui/index.html
 * 
 * @author wilelb
 */
public class MscrClientTest {
    
    private final static Logger logger = LoggerFactory.getLogger(MscrClientTest.class);
    
    private final static String CONFIG_FILE_LOCATION = "/Users/wilelb/.config/mscr/config.properties";
    
    private final String testHost = "127.0.0.1";
    private final int testPort = 8888;
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    private final Properties props = new Properties();
    
    private MSCRMockServer apiMock;
    
    public MscrClientTest() throws FileNotFoundException, IOException {
        props.load(new FileInputStream(CONFIG_FILE_LOCATION));
    }
    

    @Before
    public void setUp() throws IOException, Exception {
        apiMock = new MSCRMockServer();
        apiMock.registerSchemaSearch(
            "CLARIN", 
            new MSCRMockServer.SchemaSearchDTOBuilder()
                    .addSchema()
                    .build()
        );
        MSCRMockServer.SearchDTO crosswalk = 
            new MSCRMockServer.CrosswalkSearchDTOBuilder()
                .addSchema()                
                .build();
        apiMock.registerCrosswalkSearch("21.T13999/EOSC-202411000283272-21.T13999/EOSC-202411000283352", crosswalk);
        apiMock.registerCrosswalkSearch("Minimal tei to dc", crosswalk);
        
        apiMock.start(testPort);
    }

    @After
    public void tearDown() throws IOException {
        if(apiMock != null) {
            apiMock.shutdown();
        }
    }
    
    public MscrApiConfiguration getConfig() {
        return new MscrApiConfiguration("http://"+testHost+":"+testPort+"/v2/", "test-api-key");
    }
    
    @Test
    public void testSchemaSearch() throws UnsupportedEncodingException, MscrApiException {
        MscrClient client = new MscrClientImpl(getConfig());
        List<MscrSchema> result = client.searchSchema(new SearchBuilder()
                .type(SearchBuilder.Type.SCHEMA)
                .query("CLARIN")
                .namespace("http://test.com")
        );
        Assert.assertEquals(true, !result.isEmpty());
    }
    
    @Test
    public void testCrosswalkSearchByName() throws UnsupportedEncodingException, MscrApiException {
        MscrClient client = new MscrClientImpl(getConfig());
        List<MscrCrosswalk> result = client.searchCrosswalk(new SearchBuilder()
                .type(SearchBuilder.Type.CROSSWALK) 
                .query("Minimal tei to dc"));
        Assert.assertEquals(true, !result.isEmpty());
    }
    
    @Test
    public void testCrosswalkSearchByIds() throws UnsupportedEncodingException, MscrApiException {
        MscrClient client = new MscrClientImpl(getConfig());
        List<MscrCrosswalk> result = client.searchCrosswalk(new SearchBuilder()
                .type(SearchBuilder.Type.CROSSWALK) 
                .sourceSchema("21.T13999/EOSC-202411000283272")
                .targetSchema("21.T13999/EOSC-202411000283352"));
        Assert.assertEquals(true, !result.isEmpty());
    }
    /*
    @Test
    public void testSchemaUpload() throws FileNotFoundException, IOException, MscrApiException {
        MscrSchemaUploadRequest uploadMetadata =
            MscrSchemaUploadRequest.builder()
                .addNamespace("http://test2.com")                
                .addLabel("en", "tei to dc - test")
                .addLanguage("en")
                .build();

        File file = new File("/Users/wilelb/Code/work/clarin/git/vcr-fc4e/crosswalks/tei_minimal.xsd");
       
        MscrClient client = new MscrClientImpl(getConfig());
        String newSchemaPid = client.uploadSchema(uploadMetadata, file);
        
        Assert.assertNotNull(newSchemaPid);
    }
*/

/*    
    //@Test
    public void testSchemaRemoval() {
        //MscrClient client = new MscrClientImpl(new MscrApiConfiguration(props.getProperty("api.key")));
        //client.remmoveSchema("21.T13999/EOSC-202411000283272");
        //client.remmoveSchema("21.T13999/EOSC-202410000279527");
        //client.remmoveSchema("21.T13999/EOSC-202411000283281");
        //client.remmoveSchema("21.T13999/EOSC-202411000283352");
    }
*/

/*
    @Test
    public void testCrosswalkUpload() throws FileNotFoundException, IOException, JsonProcessingException, MscrApiException {
        MscrCrosswalkUploadRequest uploadMetadata = 
            MscrCrosswalkUploadRequest.builder()
                .addLabel("en", "tei minimal to dc")
                .addLanguage("en") 
                .setSourceSchema("mscr:schema:76d163d7-8e62-49d0-b273-0874f8eb068a") // 21.T13999/EOSC-202411000283352 --> mscr:schema:76d163d7-8e62-49d0-b273-0874f8eb068a
                .setTargetSchema("mscr:schema:84ae53c0-ca68-42af-843a-54330cef85e4") // 21.T13999/EOSC-202411000283272 --> mscr:schema:84ae53c0-ca68-42af-843a-54330cef85e4
                .setFormat(MscrCrosswalkUploadRequest.CrosswalkFormat.XSLT)
                .build();
        
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(uploadMetadata);
        
        File file = new File("/Users/wilelb/Code/work/clarin/git/vcr-fc4e/crosswalks/tei_minimal_to_dc.xslt");        
        
        MscrApiConfiguration mscrApiConfig =    
            new MscrApiConfiguration(
                props.getProperty("api.url"), 
                props.getProperty("api.key"));
        
        MscrClient client = new MscrClientImpl(mscrApiConfig);
        client.uploadCrosswalk(uploadMetadata, file);
    }
*/

/*    
    @Test
    public void testXsltDownload() throws MscrApiException {
        //String crosswalkId =  "21.T13999/EOSC-202411000284208";  //UI Generated
        String crosswalkId = "21.T13999/EOSC-202411000283922"; //Upload XSLT
        MscrClient client = new MscrClientImpl(getConfig());
        
        MscrCrosswalkMetadata crosswalk = client.fetchCrosswalk(crosswalkId);
        Assert.assertNotNull(crosswalk);
        Assert.assertTrue(crosswalk.files.length > 0);
        
        String xslt = client.fetchCrosswalkFile(crosswalkId, crosswalk.files[0].fileID);
        Assert.assertNotNull(xslt);
    }
*/

/*    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DtrResponse {
        @JsonProperty(value = "references")
        public DtrReference[] references;
    }
*/

/*    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DtrReference {
        @JsonProperty(value = "referenceName")
        public String name;
        @JsonProperty(value = "referenceURL")
        public String url;
    }
*/
    /*
    @Test
    public void performParsingDtr() {
        final Client client = ClientBuilder.newClient(); 
        final WebTarget webTarget = client.target("https://typeregistry.lab.pidconsortium.net").path("objects/21.T11969/710b1a3647d431e205e0");
        final DtrResponse response = webTarget.request(MediaType.APPLICATION_JSON).get(DtrResponse.class);
        for(DtrReference ref : response.references) {
            if(ref.name.equalsIgnoreCase("CROSSWALK")) {
                return ref.url;
            }
            System.out.println("Reference: "+ref.name+", "+ref.url);
        }        
    }
*/
}
