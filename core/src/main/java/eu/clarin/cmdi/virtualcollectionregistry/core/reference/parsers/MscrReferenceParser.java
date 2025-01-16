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
package eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers;

//import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.dtr.DtrClient;
//import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.dtr.DtrClientImpl;
/*
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrClient;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrClientImpl;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrCrosswalk;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrCrosswalkMetadata;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrNotFoundException;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrSchema;
*/
import eu.clarin.cmdi.mscr.client.lib.MscrApiConfiguration;
import eu.clarin.cmdi.mscr.client.lib.MscrApiException;
import eu.clarin.cmdi.mscr.client.lib.MscrClient;
import eu.clarin.cmdi.mscr.client.lib.MscrCrosswalk;
import eu.clarin.cmdi.mscr.client.lib.MscrCrosswalkMetadata;
import eu.clarin.cmdi.mscr.client.lib.MscrSchema;
import eu.clarin.cmdi.mscr.client.lib.SearchBuilder;
import eu.clarin.cmdi.mscr.client.lib.impl.MscrClientImpl;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.ParserConfig;
import eu.clarin.dtr.client.DtrClient;
import eu.clarin.dtr.client.DtrClientConfig;
import eu.clarin.dtr.client.DtrClientImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Try to parse any unkown metadata schema via the MSCR crosswalk functionality
 * 
 * Clear resource scan database state
 * 
delete from resource_scan_log_kv;
delete from resource_scan_log;
delete from resource_scan;
 * 
 * 
select s.id, s.ref, l.processor, v.k, v.v 
from resource_scan s 
join resource_scan_log l on s.id = l.scan_id 
join resource_scan_log_kv v on l.id = v.scan_log_id;
 * 
 * @author wilelb
 */
public class MscrReferenceParser implements ReferenceParser {
    private final static String PARSER_ID = "PARSER_MSCR";
    
    private final static Logger logger = LoggerFactory.getLogger(MscrReferenceParser.class);
    
    private MscrClient client;
    private DtrClient dtrClient;
    
    private final Map<String, String> namespaceUriToQueryMap = new HashMap<>();
    
    private MscrSchema targetSchema = null;
    
    private ReferenceParserResult result = new ReferenceParserResult();
            
    private final ParserConfig config;
    
    private final TransformerFactory transformerFactory;
    private final DocumentBuilderFactory documentFactory;
    
    public MscrReferenceParser(ParserConfig config) {
        this.config = config;
        MscrApiConfiguration mscrApiConfig = new MscrApiConfiguration(config.getMscrApiUrl(), null);
        client = new MscrClientImpl(mscrApiConfig);
        
        dtrClient = new DtrClientImpl(new DtrClientConfig(config.getDtrApiUrl()));
        
        namespaceUriToQueryMap.put("http://www.tei-c.org/ns/1.0", "TEI minimal");
        
        System.setProperty("javax.xml.transform.TransformerFactory", config.getTransformerFactory());
        this.transformerFactory = TransformerFactory.newInstance();
        this.documentFactory =  DocumentBuilderFactory.newInstance();
        
        try {
            List<MscrSchema> targetSchemas = client.searchSchema(new SearchBuilder()
                .type(SearchBuilder.Type.SCHEMA)
                .query(config.getTargetSchemaQuery()));            
            if(targetSchemas.isEmpty()) {
                throw new NoSuchElementException();
            }
            //targetSchema = targetSchemas.getFirst();
            targetSchema = targetSchemas.get(0);
        } catch(NoSuchElementException | IOException | MscrApiException ex) {
            logger.error("Target schema (query="+config.getTargetSchemaQuery()+") not found in MSCR");
        }
    }
    
    @Override
    public String getId() {
        return PARSER_ID;
    }
    
    protected String getQueryForNamespace(String xmlNamespace) {
        return null;
    }
    
    @Override
    public boolean parse(String xml, String mimeType) throws Throwable {
        result = new ReferenceParserResult();
        if(mimeType.toLowerCase().startsWith("text/xml")) {
            if(config.isMscrParserWithDtrExtendedTypesEnabled()) {
                performParsingDtr(xml, mimeType);
            } else {
                performParsing(xml, mimeType);
            }                        
        }
        return true;
    }

    @Override
    public ReferenceParserResult getResult() {
        return result;
    }
    
    protected String getNamespaceUriFromXml(String xmlContent) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {
        logger.debug("getNamespaceUriFromXml");        
        documentFactory.setNamespaceAware(true); 
        DocumentBuilder builder = documentFactory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
        
        String prefix = doc.getPrefix();
        String namespaceUri = doc.lookupNamespaceURI(prefix);
        logger.debug("namespace="+namespaceUri);
        return namespaceUri;
    }
    
    protected String getCrosswalkIdFromUrl(String url) {
        URI baseApiUrl = URI.create(config.getMscrApiUrl());
        return url.replace(baseApiUrl.resolve("crosswalk").toString()+"/", "");       
    }
    
    /**
     * Parse and transform the incoming XML using DTR extended mimetypes and 
     * MSCR crosswalks.
     * 
     * @param xml
     * @param mimeType
     * @return 
     */
    protected void performParsingDtr(String xml, String mimeType) {
        result.addProcessStep("performParsingDtr");
        try {
            String dataTypeId = "21.T11969/710b1a3647d431e205e0";
            result.addProcessStep("Found datatype with id: " + dataTypeId);
            
            String crosswalkApiUrl = 
                dtrClient.getExtendedTypeCrosswalk(dataTypeId);
            if(crosswalkApiUrl != null) {
                String crosswalkId = getCrosswalkIdFromUrl(crosswalkApiUrl);
                result.addProcessStep("Found crosswalk with id: " + crosswalkId);
                executeCrosswalk(xml, crosswalkId);
            }
        } catch(Throwable ex) {
            throw new RuntimeException("Failed to parse reference with DTR and MSCR. Cause: "+ex.getMessage(), ex);
        }
    }
    
    /**
     * Parse and transform the incoming XML using MSCR schema search and MSCR 
     * crosswalks.
     * 
     * @param xml
     * @param mimeType 
     */
    protected void performParsing(String xml, String mimeType) {    
        result.addProcessStep("performParsing");
        
        //Validate target schema
        if(targetSchema == null) {
            throw new RuntimeException("Target schema is required for MSCR parsing.");
        }
                
        //Search and fetch source schema
        String mscrSourceSchemaQuery = null;
        try {                
            String namespace = getNamespaceUriFromXml(xml);
            mscrSourceSchemaQuery = namespaceUriToQueryMap.get(namespace);    
            if(mscrSourceSchemaQuery == null) {
                throw new RuntimeException("No mscr query found for xml namespace ("+namespace+")");
            }   
        } catch(IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Failed to fetch XML root namespace URI.");
        }
        
        MscrSchema sourceSchema = null;
        try {
            List<MscrSchema> sourceSchemas = client.searchSchema(new SearchBuilder()
                .type(SearchBuilder.Type.SCHEMA)
                .query(mscrSourceSchemaQuery));
            if(sourceSchemas.isEmpty()) {
                throw new NoSuchElementException();
            }
            //sourceSchema = sourceSchemas.getFirst();
            sourceSchema = sourceSchemas.get(0);
        } catch(IOException | MscrApiException ex) {
            throw new RuntimeException("Failed to communicate with the MSCR API", ex);
        } catch(NoSuchElementException ex) {
            throw new RuntimeException("Source schema not found", ex);
        }
        result.addProcessStep("Found source schema: " + sourceSchema.id);
        
        //Search and fetch crosswalk
        MscrCrosswalk crosswalk = null;
        try {
            List<MscrCrosswalk> crosswalks = client.searchCrosswalk(
                new SearchBuilder()
                        .type(SearchBuilder.Type.CROSSWALK)
                        .sourceSchema(sourceSchema.source.id)
                        .targetSchema(targetSchema.source.id));
            if(crosswalks.isEmpty()) {
                throw new NoSuchElementException();
            }
            //crosswalk = crosswalks.getFirst();
            crosswalk = crosswalks.get(0);
        } catch(IOException | MscrApiException ex) {
            throw new RuntimeException("Failed to communicate with the MSCR API", ex);
        } catch(NoSuchElementException ex) {
            throw new RuntimeException("Crosswalk not found: source schema="+sourceSchema.source.id+", target schema="+targetSchema.source.id);
        }
        result.addProcessStep("Found crosswalk: " + crosswalk.id);
       
        executeCrosswalk(xml, crosswalk.source.id);
    }
    
    /**
     * Given a MSCR crosswalk, transform the xml and store the results.
     * 
     * @param xml
     * @param crosswalkId 
     */
    public void executeCrosswalk(String xml, String crosswalkId) {
        MscrCrosswalkMetadata crosswalkMetadata = null;
        try {
            crosswalkMetadata = client.fetchCrosswalk(crosswalkId);
        //} catch(IOException ex) {
          //  throw new RuntimeException("Failed to communicate with the MSCR API", ex);
        } catch(MscrApiException ex) {
            throw new RuntimeException("Crosswalk metadata not found: crosswalk id="+crosswalkId, ex);
        }
        
        //Execute crosswalk (xslt)
        if(crosswalkMetadata.files.length > 0) {
            String xslt = null;
            try {
                xslt = client.fetchCrosswalkFile(crosswalkId, crosswalkMetadata.files[0].fileID);                
            //} catch(IOException ex) {
            //    throw new RuntimeException("Failed to communicate with the MSCR API", ex);
            } catch(MscrApiException ex) {
                throw new RuntimeException("Crosswalk xslt file not found: crosswalk id="+crosswalkId+", file id="+crosswalkMetadata.files[0].fileID, ex);
            }
            
            String transformedXml = transform(xml, xslt);           
            try {
                XmlParser xmlParser = new XmlParser(transformedXml, "http://purl.org/dc/elements/1.1/");
                String title = xmlParser.getValueForXPath("//default:dc/default:title/text()");
                result.add(ReferenceParserResult.KEY_DESCRIPTION, title);
                String author = xmlParser.getValueForXPath("//default:dc/default:author/text()");
                result.add(ReferenceParserResult.KEY_NAME, author);
            } catch(SAXException | ParserConfigurationException | IOException ex) {
                throw new RuntimeException("Failed to parse transformed xml", ex);
            }
        }
    }
    
     protected String transform(String xmlContent, String xsltContent) {   
        logger.debug("Running transformation");
        
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
            logger.error("Failed to transform xml", ex);
        }
        
        logger.info(""+sw.toString());
        
        return sw.toString();
    }
        
}
