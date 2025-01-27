package eu.clarin.cmdi.mscr.client.lib.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.mscr.client.lib.MscrApiConfiguration;
import eu.clarin.cmdi.mscr.client.lib.MscrApiException;
import eu.clarin.cmdi.mscr.client.lib.MscrClient;
import eu.clarin.cmdi.mscr.client.lib.MscrCrosswalk;
import eu.clarin.cmdi.mscr.client.lib.MscrCrosswalkMetadata;
import eu.clarin.cmdi.mscr.client.lib.MscrCrosswalkUploadRequest;
import eu.clarin.cmdi.mscr.client.lib.MscrCrosswalkUploadResponse;
import eu.clarin.cmdi.mscr.client.lib.MscrSchema;
import eu.clarin.cmdi.mscr.client.lib.MscrSchemaUploadRequest;
import eu.clarin.cmdi.mscr.client.lib.MscrSchemaUploadResponse;
import eu.clarin.cmdi.mscr.client.lib.MscrSearchResult;
import eu.clarin.cmdi.mscr.client.lib.SearchBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;


/**
 *
 * @author wilelb
 */
public class MscrClientImpl implements MscrClient {

    //private final static Logger logger = LoggerFactory.getLogger(MscrClientImpl.class);
    private final Logger logger = Logger.getLogger(getClass().getName());
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    private final MscrApiConfiguration config;    
    
    public MscrClientImpl(MscrApiConfiguration config) {
        this.config = config;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
     * Get a webtarget.
     * 
     * @param config
     * @param path
     * @return 
     */
    protected WebTarget getWebtarget(final MscrApiConfiguration config, final String path) {        
        final Client client = ClientBuilder.newClient(
            new ClientConfig()
                    .register(MultiPartFeature.class)
                    .register(JacksonFeature.class)); 
        //client.register(new LoggingFeature(logger, Level.INFO, null, null));
        return client.target(config.getServiceBaseURL()).path(path);
    }
    
    protected Response validateResponse(Response response, int[] expectedResponseCodes) throws MscrApiException {
        int status = response.getStatus();
        
        for(int validCode : expectedResponseCodes) {
            if(status == validCode) {
                return response;
            }
        }
        
        String body = response.readEntity(String.class);
        throw new MscrApiException(status, body);        
    }
    
    @Override
    public List<MscrSchema> searchSchema(SearchBuilder search) throws MscrApiException, UnsupportedEncodingException {
        search.type(SearchBuilder.Type.SCHEMA);
        final WebTarget webTarget = 
            search.addQueryParameters(
                getWebtarget(config, "/frontend/mscrSearch"));
        
        final Response response = validateResponse(
            webTarget.request(MediaType.APPLICATION_JSON).get(),
                new int[] {HttpURLConnection.HTTP_OK});
        
        List<MscrSchema> result = new ArrayList<>();
        try {
            MscrSearchResult<MscrSchema> searchResult = 
                mapper.readValue(
                        response.readEntity(String.class), 
                            mapper.getTypeFactory().constructParametricType(MscrSearchResult.class, MscrSchema.class));
            result = searchResult.hits.hits;
        } catch(JsonProcessingException ex) {
            throw new MscrApiException(response.getStatus(), "Failed to parse API response", ex);
        }
        return result;
    }
    
    @Override
    public List<MscrCrosswalk> searchCrosswalk(SearchBuilder search) throws MscrApiException, UnsupportedEncodingException {
        search.type(SearchBuilder.Type.CROSSWALK);
        final WebTarget webTarget = 
            search.addQueryParameters(
                getWebtarget(config, "/frontend/mscrSearch"));
        
        final Response response = validateResponse(
            webTarget.request(MediaType.APPLICATION_JSON).get(),
                new int[] {HttpURLConnection.HTTP_OK});
        
        List<MscrCrosswalk> result = new ArrayList<>();
        try {
            MscrSearchResult<MscrCrosswalk> searchResult = 
                mapper.readValue(
                        response.readEntity(String.class), 
                        mapper.getTypeFactory().constructParametricType(MscrSearchResult.class, MscrCrosswalk.class));
            result = searchResult.hits.hits;
        } catch(JsonProcessingException ex) {
            throw new MscrApiException(response.getStatus(), "Failed to parse API response", ex);
        }

        return result;
    }
    
    @Override
    public String uploadSchema(MscrSchemaUploadRequest uploadMetadata, File file) throws JsonProcessingException, MscrApiException {
        final FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.field("metadata", mapper.writeValueAsString(uploadMetadata));
        multiPart.bodyPart(new FileDataBodyPart("file", file));
        
        final Response response = validateResponse(
            getWebtarget(config, "/schemaFull")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer "+config.getApiKey())            
                    .put(Entity.entity(multiPart, multiPart.getMediaType())),
            new int[] {HttpURLConnection.HTTP_OK});       
        
        MscrSchemaUploadResponse body = response.readEntity(MscrSchemaUploadResponse.class);
        return body.handle != null ? body.handle : body.pid;
    }
    
    @Override
    public boolean remmoveSchema(String schemaPid) throws MscrApiException {
        validateResponse(
        getWebtarget(config, "/schema/"+schemaPid)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer "+config.getApiKey())            
                .delete(),
            new int[] {HttpURLConnection.HTTP_OK});       
        
        return true;
    }
    
    @Override
    public String uploadCrosswalk(MscrCrosswalkUploadRequest uploadMetadata, File file) throws JsonProcessingException, MscrApiException {
        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.field("metadata", mapper.writeValueAsString(uploadMetadata));
        multiPart.bodyPart(new FileDataBodyPart("file", file));
        
        final Response response = validateResponse(
            getWebtarget(config, "/crosswalkFull")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer "+config.getApiKey())            
                    .put(Entity.entity(multiPart, multiPart.getMediaType())),
            new int[] {HttpURLConnection.HTTP_OK});
        
        MscrCrosswalkUploadResponse body = response.readEntity(MscrCrosswalkUploadResponse.class);
        return body.handle != null ? body.handle : body.pid;
    }
    
    @Override
    public MscrCrosswalkMetadata fetchCrosswalk(String crosswalkId) throws MscrApiException {
        final WebTarget webTarget = getWebtarget(config, "/crosswalk/"+crosswalkId);
        final Response response = 
            webTarget.request(MediaType.APPLICATION_JSON).get();
        
        MscrCrosswalkMetadata crosswalk = response.readEntity(MscrCrosswalkMetadata.class);
        return crosswalk;
    }
    
    @Override
    public String fetchCrosswalkFile(String crosswalkId, String fileId) throws MscrApiException {
        final WebTarget webTarget = 
            getWebtarget(config, "/crosswalk/"+crosswalkId+"/files/"+fileId)
                .queryParam("download", true);
        final Response response = 
            webTarget.request(MediaType.APPLICATION_JSON).get();
        
        String body = response.readEntity(String.class);
        return body;
    }
}
