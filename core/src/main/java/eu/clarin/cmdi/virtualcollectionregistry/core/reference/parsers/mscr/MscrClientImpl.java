/*
 * Copyright (C) 2024 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author wilelb
 */
public class MscrClientImpl implements MscrClient {
    
    private final static Logger logger = LoggerFactory.getLogger(MscrClientImpl.class);
    
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    
    private final URI baseApiUrl = 
            URI.create("https://mscr-test.rahtiapp.fi/datamodel-api/v2/");
    
    private final RequestConfig requestConfig;
    
    private final DocumentBuilderFactory documentFactory;
    private final TransformerFactory transformerFactory;
    
    /**
     * MSCR search response handler. Process the search result and return the
     * expected types passed as the T parameter.
     * 
     * @param <T> 
     */
    public static class MscrSearchResponseHandler<T> extends JsonResponseHandler<T> {
        public MscrSearchResponseHandler(T content) {
            super(content);
        }
        
        @Override
        public T getResponse() throws JsonProcessingException {
            Class<T> clazz = (Class<T>)content.getClass();
            MscrSearch search = mapper.readValue(body, MscrSearch.class);
            if(search.hits.total.value > 0) {
                return mapper.convertValue(search.hits.hits, clazz);
            }
            
            return null;
        }
    }

    /**
     * Basic response handler with support to unmarshall the  JSON response into
     * the type of the class generic variable T.
     * 
     * TODO: somehow infer the proper class from the class generic type T so it 
     * is not needed to pass an instance in the constructor.
     * 
     * @param <T> 
     */
    public static class JsonResponseHandler<T> extends BasicResponseHandler {
        protected T content;

        public JsonResponseHandler(T content) {
            this.content = content;
        }

        public T getResponse() throws JsonProcessingException {
            Class<T> clazz = (Class<T>)content.getClass();
            return mapper.readValue(body, clazz);
        }
    }
    
    /**
     * Handle the response and store the entiry content as a string.
     */
    public static class BasicResponseHandler implements ResponseHandler<String> {    
        protected final static ObjectMapper mapper = new ObjectMapper();

        protected String body;
        
        public BasicResponseHandler() {}
        
        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            int httpCode = response.getStatusLine().getStatusCode();
            if (httpCode >= 200 && httpCode < 300) {
                HttpEntity entity = response.getEntity();
                body = entity != null ? EntityUtils.toString(entity) : null;
            }
            return body;
        }

        public String getBody() {
            return this.body;
        }
    }
    
    public MscrClientImpl() {
        this.requestConfig =
            RequestConfig
                .custom()
                .setConnectionRequestTimeout(1000)
                .setMaxRedirects(5)
                .build();
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        this.transformerFactory = TransformerFactory.newInstance();
        this.documentFactory = DocumentBuilderFactory.newInstance();
    }
    
    protected void issueGet(String apiPath, ResponseHandler responseHandler) throws IOException {        
        issueRequest(new HttpGet(
            baseApiUrl.resolve(apiPath)), responseHandler);
    }
    
    protected void issuePost(String apiPath, ResponseHandler responseHandler) throws IOException {        
        issueRequest(new HttpPost(
            baseApiUrl.resolve(apiPath)), responseHandler);
    }
    
    protected void issueRequest(HttpRequestBase httpReq, ResponseHandler responseHandler) throws IOException {
        httpReq.setConfig(requestConfig);
        httpclient.execute(httpReq, responseHandler);          
    }
    
    @Override
    public MscrSchema searchSchema(String query) throws MscrNotFoundException, IOException {
        List<MscrSchema> schemas = searchSchemas(query);
        if(!schemas.isEmpty()) {
            return schemas.get(0);
        }
        return null;
    }
    
    @Override
    public List<MscrSchema> searchSchemas(String query) throws MscrNotFoundException, IOException {
        List<MscrSchema> result = new ArrayList<>();
        try {
            MscrSearchResponseHandler<MscrSchema[]> responseHandler = 
                    new MscrSearchResponseHandler<>(new MscrSchema[0]);
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String path = "./frontend/mscrSearch?query="+encodedQuery+"&type=SCHEMA";
            issueGet(path, responseHandler);
            MscrSchema[] r = responseHandler.getResponse();
            //Todo: how to handle multiple results
            for(MscrSchema schema : r) {
                String label = schema.source.getLabel("en");
                if(label.equalsIgnoreCase(query)) {
                    result.add(schema);
                }
            }
        } catch(UnsupportedEncodingException | JsonProcessingException ex) {
            logger.error("", ex);
        }
        return result;
    }
    
    @Override
    public MscrCrosswalk searchCrosswalk(String sourceSchemaId, String targetSchemaId) throws MscrNotFoundException, IOException {
        List<MscrCrosswalk> crosswalks = searchCrosswalks(sourceSchemaId, targetSchemaId);
        if(!crosswalks.isEmpty()) {
            return crosswalks.get(0);
        }
        return null;
    }
    
    @Override
    public List<MscrCrosswalk> searchCrosswalks(String sourceSchemaId, String targetSchemaId) throws MscrNotFoundException, IOException {
        List<MscrCrosswalk> result = new ArrayList<>();
        try {
            MscrSearchResponseHandler<MscrCrosswalk[]> responseHandler = 
                    new MscrSearchResponseHandler<>(new MscrCrosswalk[0]);
            String path = "./frontend/mscrSearch"+
                    "?sourceSchemas="+sourceSchemaId+
                    "&targetSchemas="+targetSchemaId+
                    "&type=CROSSWALK";
            issueGet(path, responseHandler);
            MscrCrosswalk[] r = responseHandler.getResponse();
            result = Arrays.asList(r);
        } catch(JsonProcessingException ex) {
            logger.error("", ex);
        }
        return result;
    }
    
    @Override
    public MscrCrosswalkMetadata fetchCrosswalkMetadata(String crosswalkId) throws MscrNotFoundException, IOException {
        MscrCrosswalkMetadata md = null;
        try {
            JsonResponseHandler<MscrCrosswalkMetadata> responseHandler = 
                    new JsonResponseHandler<>(new MscrCrosswalkMetadata());
            String path = "./crosswalk/"+crosswalkId;
            issueGet(path, responseHandler);
            md = responseHandler.getResponse();
            logger.info("Md: "+md.toString());
        } catch(JsonProcessingException ex) {
            logger.error("", ex);
        }
        return md;
    }
    
    @Override
    public String fetchCrosswalkXslt(String crosswalkId, String fileId) throws MscrNotFoundException, IOException {
        BasicResponseHandler responseHandler = new BasicResponseHandler();
        String path = "./crosswalk/"+crosswalkId+"/files/"+fileId+"?download=true";
        issueGet(path, responseHandler);
        return responseHandler.getBody();
    }
    /*
    @Override
    public String getUrlContent(String url) throws IOException {
        BasicResponseHandler responseHandler = new BasicResponseHandler();
        issueRequest(new HttpGet(url), responseHandler);
        return responseHandler.getBody();
    }
    */
    @Override
    public String transformFromUrl(String url, String xslt) throws IOException {
        BasicResponseHandler responseHandler = new BasicResponseHandler();
        issueRequest(new HttpGet(url), responseHandler);
        return transform(responseHandler.getBody(), xslt);
    }
    
    /**
     * https://stackoverflow.com/questions/40181386/how-to-run-saxon-xslt-transformation-in-java
     * https://stackoverflow.com/questions/30321882/xsl-transformation-of-an-input-xml-using-java
     * @param xmlContent
     * @param xsltContent
     * @return 
     */
    @Override
    public String transform(String xmlContent, String xsltContent) {   
        StringWriter sw = new StringWriter();        
        try {
            // Build a transformer from the XSLT
            Source xslt = new StreamSource(new ByteArrayInputStream(xsltContent.getBytes("UTF-8")));
            Transformer transformer = transformerFactory.newTransformer(xslt);
            transformer.setURIResolver(null);
            
            // Transform the xmlContent            
            Source source = new StreamSource(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
        } catch (TransformerException | IOException ex) {
            logger.error("", ex);
        }
        
        return sw.toString();
    }
    
    
    @Override
    public String getNamespaceUriFromXml(String xmlContent) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {
        documentFactory.setNamespaceAware(true); 
        DocumentBuilder builder = documentFactory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
        
        String prefix = doc.getPrefix();
        String namespaceUri = doc.lookupNamespaceURI(prefix);
        return namespaceUri;
    }
}