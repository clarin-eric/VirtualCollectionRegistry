/*
Mock the api specified with the shared specification to be used in java unit tests. 
Make sure to implement the endpoints to search for schemas, upload schemas, search for crosswalks, 
upload crosswalks, download crosswalks and download crosswalk files.

Use the okhttp3 mockwebserver library and do not use the springframework.
Also provide implementation for the DTO classes.

Use the provided specification

<copy paste the api spec: https://mscr-test.2.rahtiapp.fi/datamodel-api/v3/api-docs >
 */
package eu.clarin.cmdi.mscr.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MSCRMockServer {
    private final static Logger logger = LoggerFactory.getLogger(MSCRMockServer.class);
    
    private final MockWebServer server;
    private final ObjectMapper objectMapper;

    private final Map<String, SearchDTO> schemaSearches = new HashMap<>();
    private final Map<String, SearchDTO> crosswalkSearches = new HashMap<>();
    
    public MSCRMockServer() throws Exception {
        this.server = new MockWebServer();
        this.objectMapper = new ObjectMapper();
        OpenAPI openAPI = parseOpenAPISpec("https://mscr-test.2.rahtiapp.fi/datamodel-api/v3/api-docs");
        setupDispatcher(openAPI);
    }
    
    public void start(int port) throws IOException {
        server.start(port);
    }

    public void shutdown() throws IOException {
        server.shutdown();
    }

    public String getBaseUrl() {
        return server.url("/").toString();
    }
    
    public MSCRMockServer registerSchemaSearch(String search, SearchDTO result) throws UnsupportedEncodingException {
        String normalizedSearch = URLDecoder.decode(URLEncoder.encode(search, "UTF-8"), "UTF-8");
        normalizedSearch = normalizedSearch.replaceAll(" ", "+");
        schemaSearches.put(normalizedSearch, result);
        return this;
    }
    
    public MSCRMockServer registerCrosswalkSearch(String search, SearchDTO result) throws UnsupportedEncodingException {
        String normalizedSearch = URLDecoder.decode(URLEncoder.encode(search, "UTF-8"), "UTF-8");
        normalizedSearch = normalizedSearch.replaceAll(" ", "+");
        crosswalkSearches.put(normalizedSearch, result);
        return this;
    }
    
    private OpenAPI parseOpenAPISpec(String specUrl) throws Exception {
        SwaggerParseResult result = new OpenAPIParser().readLocation(specUrl, null, null);
        OpenAPI openAPI = result.getOpenAPI();
  
        // validation errors and warnings
        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            for(String m : result.getMessages()) {
                logger.warn(m);
            }
            /*
            String msg = result.getMessages().getFirst();
            if(result.getMessages().size() > 1) {
                msg += " ("+(result.getMessages().size()-1)+" more)";
            }            
            throw new Exception("Failed to parse open API specification: "+msg);
            */
        } 
  
        if (openAPI == null) {
            throw new Exception("Failed to parse open API specification (result = null)");
        }
        
        return openAPI;
    }

    private void setupDispatcher(OpenAPI openAPI) {
        /*
        Paths paths = openAPI.getPaths();
        for(String key : paths.keySet()) {
            PathItem pathItem = paths.get(key);
            Map<HttpMethod, Operation> opsMap = pathItem.readOperationsMap();
            for(HttpMethod opMethod : opsMap.keySet() ) {
                Operation op = opsMap.get(opMethod);
                
                logger.info(opMethod.name() + ":  " + key);
                if(op.getParameters() != null) {
                    for(Parameter param : op.getParameters()) {
                        logger.info("    Name={}, style={}", param.getName(), param.getStyle());
                    }
                }
            }
        }
        */
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                String method = request.getMethod();

                try {
                    // Crosswalk endpoints
                    if (path.startsWith("/v2/crosswalk")) {
                        return handleCrosswalkRequests(request, path, method);
                    }
                    // Frontend endpoints
                    if (path.startsWith("/v2/frontend")) {
                        return handleFrontendRequests(request, path, method);
                    }
                } catch(UnsupportedEncodingException | JsonProcessingException ex) {
                    logger.error("", ex); 
                    return new MockResponse().setResponseCode(500);
                }
                
                return new MockResponse().setResponseCode(404);
            }
        };
        server.setDispatcher(dispatcher);        
    }

    private MockResponse handleCrosswalkRequests(RecordedRequest request, String path, String method) {
        // GET /crosswalk/"+crosswalkId
        // GET /v2/crosswalk/{pid}
        Pattern getCrosswalkPattern = Pattern.compile("/v2/crosswalk/([^/]+)$");
        Matcher getCrosswalkMatcher = getCrosswalkPattern.matcher(path);
        if (getCrosswalkMatcher.matches() && "GET".equals(method)) {
            String pid = getCrosswalkMatcher.group(1);
            //return??
        }
        
        // GET /crosswalk/"+crosswalkId+"/files/"+fileId+"?download=true"
        Pattern getFilesPattern = Pattern.compile("/v2/crosswalk/([^/]+)/files/([^/]+)\\?download=(true|false)$");
        Matcher getFilesMatcher = getCrosswalkPattern.matcher(path);
        if (getCrosswalkMatcher.matches() && "GET".equals(method)) {
            String pid = getCrosswalkMatcher.group(1);
            String fileId = getCrosswalkMatcher.group(2);
            String download = getCrosswalkMatcher.group(3);
            //return ??
        }
        
        return new MockResponse().setResponseCode(404);
    }

    // GET /frontend/mscrSearch?query="+encodedQuery+"&type=SCHEMA"
    // GET /frontend/mscrSearch?sourceSchemas=sourceSchemaId&targetSchemas="+targetSchemaId&type=CROSSWALK"
    private MockResponse handleFrontendRequests(RecordedRequest request, String path, String method) throws UnsupportedEncodingException, JsonProcessingException {
        // GET /v2/frontend/mscrSearch
        if (path.startsWith("/v2/frontend/mscrSearch") && "GET".equals(method)) {
            Map<String, String> params = parseQueryParameters(path);
            
            SearchDTO schema = null;
            String type = params.get("type");
            if("SCHEMA".equals(type)) {
                String query = params.get("query");
                schema = schemaSearches.get(query);
            } else if("CROSSWALK".equals(type)) {
                String key = params.get("query");
                String sourceSchemas = params.get("sourceSchemas");
                String targetSchemas = params.get("targetSchemas");
                if(key == null) {
                    key = sourceSchemas+"-"+targetSchemas;
                }                        
                schema = crosswalkSearches.get(key);
            }
            
            if(schema == null) {
                return new MockResponse().setResponseCode(404);
            }

            String json = objectMapper.writeValueAsString(schema);
            return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(json);
        }

        return new MockResponse().setResponseCode(404);
    }
    
    private Map<String, String> parseQueryParameters(String path) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<>();
        
        String decodedPath = URLDecoder.decode(path, "UTF-8");
        Pattern regex = Pattern.compile("[\\?]?([\\w]+)=([\\w\\.\\-\\:\\/ \\+]+)");
        Matcher m = regex.matcher(decodedPath);
        while(m.find()) {
            String name = m.group(1);
            String value = m.group(2);
            if(name != null) {
                result.put(name, value);
            }
        }
        
        return result;
    }
    
   
    public static class SchemaSearchDTOBuilder {
        private final SearchDTO search = new SearchDTO();
        private SchemaDTO currentSchema = new SchemaDTO();
        
        public SchemaSearchDTOBuilder() {
            search.hits = new SearchHits();
            search.hits.hits = new ArrayList();
            search.hits.total = new SearchHitsTotal();
            search.hits.total.relation = "eq";
            search.hits.total.value = 0;
        }
        
        public SchemaSearchDTOBuilder addSchema() {
            search.hits.hits.add(currentSchema);
            search.hits.total.value += 1;
            currentSchema = new SchemaDTO();
            return this;
        }
        
        public SearchDTO build() {
            return search;
        }
    }
    
    public static class CrosswalkSearchDTOBuilder {
        private final SearchDTO search = new SearchDTO();
        private CrosswalkInfoDTO currentCrosswalk = new CrosswalkInfoDTO();
        
        public CrosswalkSearchDTOBuilder() {
            search.hits = new SearchHits();
            search.hits.hits = new ArrayList();
            search.hits.total = new SearchHitsTotal();
            search.hits.total.relation = "eq";
            search.hits.total.value = 0;
        }
        
        public CrosswalkSearchDTOBuilder addSchema() {
            search.hits.hits.add(currentCrosswalk);
            search.hits.total.value += 1;
            currentCrosswalk = new CrosswalkInfoDTO();
            return this;
        }
        
        public SearchDTO build() {
            return search;
        }
    }
        
    public static class SearchDTO {
        public int took;
        public boolean timedOut;
        public SearchHits hits;
    }
    
    public static class SearchHits {
        public SearchHitsTotal total;
        public List hits;
    }
    
    public static class SearchHitsTotal {
        public String relation;
        public int value;
    }
    
    public static class SchemaDTO {
        public String prefix;
        public String status;
        public Map<String, String> label;
        public Map<String, String> description;
        public List<String> languages;
        public List<UUID> organizations;
        public List<String> groups;
        public String state;
        public String visibility;
        public String format;
        public String namespace;
        public String versionLabel;
        public String sourceURL;

        // Getters and setters
        // ... standard implementations
    }

    public static class SchemaInfoDTO extends SchemaDTO {
        public String created;
        public String modified;
        public UserDTO modifier;
        public UserDTO creator;
        public String type;
        public String handle;
        public List<FileMetadata> fileMetadata;
        public String pid;
    }

    public static class CrosswalkDTO {
        public String prefix;
        public String status;
        public Map<String, String> label;
        public Map<String, String> description;
        public List<String> languages;
        public List<UUID> organizations;
        public String state;
        public String visibility;
        public String format;
        public String sourceSchema;
        public String targetSchema;
        public String versionLabel;
        public String sourceURL;
    }

    public static class CrosswalkInfoDTO extends CrosswalkDTO {
        public String created;
        public String modified;
        public UserDTO modifier;
        public UserDTO creator;
        public String type;
        public String prefix;
        public String status;
        public String handle;
        public List<FileMetadata> fileMetadata;
        public String pid;
        public List<MappingDTO> mappings;

    }
    
    public class UserDTO  {
        public String id;
        public String name;
    }
    
    public class FileMetadata {
        public String contentType;
        public int size;
        public long fileID;
        public String filename;
        public String timestamp;
    }
    
    public class MappingDTO {
        public String id;
        //depends_on?
        //source?
        public String sourceType;
        public String sourceDescription;
        public String predicate;
        public MappingFilterDTO filter;
        //target?
        public String targetType;
        public String targetDescription;
        public ProcessingInfo processing;
        public String notes;
        //oneOf
    }
    
    public class MappingInfoDTO {
        public String id;
        //depends_on?
        //source?
        public String sourceType;
        public String sourceDescription;
        public String predicate;
        public MappingFilterDTO filter;
        //target?
        public String targetType;
        public String targetDescription;
        public ProcessingInfo processing;
        public String notes;
        //oneOf
        public String isPartOf;
        public String pid;
    }
    
    public class MappingFilterDTO {
        public String path;
        public String operator;
        //value?
        public boolean distinctValues;
    }
    
    public class ProcessingInfo {
        public String id;
        //params?
    }
}