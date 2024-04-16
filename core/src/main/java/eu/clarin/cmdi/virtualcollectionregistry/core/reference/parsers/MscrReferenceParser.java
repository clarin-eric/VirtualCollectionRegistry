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

import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrClient;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrClientImpl;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrCrosswalk;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrCrosswalkMetadata;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrNotFoundException;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrSchema;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.ParserConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private final Map<String, String> namespaceUriToQueryMap = new HashMap<>();
    
    private MscrSchema targetSchema = null;
    
    private ReferenceParserResult result = new ReferenceParserResult();
            
    public MscrReferenceParser(ParserConfig config) {
        client = new MscrClientImpl(
                config.getApiUrl(), 
                config.getConnectionRequestTimeout(), 
                config.getMaxRedirects(), 
                config.getTransformerFactory());
        
        namespaceUriToQueryMap.put("http://www.tei-c.org/ns/1.0", "TEI minimal");
        
        try {
            targetSchema = client.searchSchema(config.getTargetSchemaQuery());
        } catch(MscrNotFoundException | IOException ex) {
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
    public boolean parse(String xml, String mimeType) throws Exception {
        result = new ReferenceParserResult();
        boolean handled = false;
        if(mimeType.toLowerCase().startsWith("text/xml")) {
            performParsing(xml, mimeType);
            handled = true;
        }
        return handled;
    }

    @Override
    public ReferenceParserResult getResult() {
        return result;
    }
   
    protected void performParsing(String xml, String mimeType) {       
        //Validate target schema
        if(targetSchema == null) {
            throw new RuntimeException("Target schema is required for MSCR parsing.");
        }
                
        //Search and fetch source schema
        String mscrSourceSchemaQuery = null;
        try {                
            String namespace = client.getNamespaceUriFromXml(xml);
            mscrSourceSchemaQuery = namespaceUriToQueryMap.get(namespace);    
            if(mscrSourceSchemaQuery == null) {
                throw new RuntimeException("No mscr query found for xml namespace ("+namespace+")");
            }   
        } catch(IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Failed to fetch XML root namespace URI.");
        }
               
        MscrSchema sourceSchema = null;
        try {
            sourceSchema = client.searchSchema(mscrSourceSchemaQuery);
        } catch(IOException ex) {
            throw new RuntimeException("Failed to communicate with the MSCR API", ex);
        } catch(MscrNotFoundException ex) {
            throw new RuntimeException("Source schema not found", ex);
        }
        
        //Search and fetch crosswalk
        MscrCrosswalk crosswalk = null;
        try {
            crosswalk = client.searchCrosswalk(sourceSchema.source.id, targetSchema.source.id);
        } catch(IOException ex) {
            throw new RuntimeException("Failed to communicate with the MSCR API", ex);
        } catch(MscrNotFoundException ex) {
            throw new RuntimeException("Crosswalk not found: source schema="+sourceSchema.source.id+", target schema="+targetSchema.source.id);
        }
        
        MscrCrosswalkMetadata crosswalkMetadata = null;
        try {
            crosswalkMetadata = client.fetchCrosswalkMetadata(crosswalk.source.id);
        } catch(IOException ex) {
            throw new RuntimeException("Failed to communicate with the MSCR API", ex);
        } catch(MscrNotFoundException ex) {
            throw new RuntimeException("Crosswalk metadata not found: crosswalk id="+crosswalk.source.id);
        }
        
        //Execute crosswalk (xslt)
        if(crosswalkMetadata.files.length > 0) {
            String xslt = null;
            try {
                xslt = client.fetchCrosswalkXslt(crosswalk.source.id, crosswalkMetadata.files[0].fileID);                
            } catch(IOException ex) {
                throw new RuntimeException("Failed to communicate with the MSCR API", ex);
            } catch(MscrNotFoundException ex) {
                throw new RuntimeException("Crosswalk xslt file not found: crosswalk id="+crosswalk.source.id+", file id="+crosswalkMetadata.files[0].fileID);
            }
            
            String transformedXml = client.transform(xml, xslt);           
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
        
}
