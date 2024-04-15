package eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers;


import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrSchema;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrCrosswalk;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrSearch;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrClient;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrClientImpl;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrCrosswalkMetadata;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 *
 * URL res1 = getClass().getResource("/mscr_schema_search_result.json");
 * URL res2 = MscrReferenceParserTest.class.getClassLoader().getResource("mscr_schema_search_result.json");
 * 
 * @author wilelb
 */
public class MscrReferenceParserTest {
    private final static ObjectMapper mapper = new ObjectMapper();
    
    private final static Logger logger = LoggerFactory.getLogger(MscrReferenceParserTest.class);
    
    @Test
    public void testParseSchemaSearch() throws IOException {
        URL res1 = getClass().getResource("/mscr_schema_search_result.json");
        MscrSearch search = mapper.readValue(res1.openStream(), MscrSearch.class);
        if(search.hits.total.value > 0) {
            MscrSchema[] schemas = mapper.convertValue(search.hits.hits, MscrSchema[].class);
            logger.info("Found {} schemas", schemas.length);
        }
    }
    
    @Test
    public void testParseCrosswalkSearch() throws IOException {
        URL res1 = getClass().getResource("/mscr_crosswalk_search_result.json");
        MscrSearch search = mapper.readValue(res1.openStream(), MscrSearch.class);
        if(search.hits.total.value > 0) {
            MscrCrosswalk[] crosswalks = mapper.convertValue(search.hits.hits, MscrCrosswalk[].class);
            logger.info("Found {} schemas", crosswalks.length);
        }
    }
    
     @Test
    public void testParseCrosswalkMetadata() throws IOException {    
        URL res1 = getClass().getResource("/mscr_crosswalk2.json");
        //MscrCrosswalkMetadata crosswalk = mapper.readValue(res1.openStream(), MscrCrosswalkMetadata.class);
        MscrCrosswalkMetadata crosswalk = mapper.readValue(res1.openStream(), new MscrCrosswalkMetadata().getClass());
        logger.info("Found {} schemas", crosswalk.format);
    }
    
    @Test
    public void test() throws Exception {
        URL res1 = getClass().getResource("/tei_example.xml");
        
        String xmlContent = new BufferedReader(
            new InputStreamReader(res1.openStream(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n")
        );
        
        MscrClient client = new MscrClientImpl();
        client.getNamespaceUriFromXml(xmlContent);
        
    }
    /**
     * Todo: mock remote responses
     * 
     * @throws Exception 
     */
    //@Test
    public void testSchemaSearch() throws Exception {
        MscrClient client = new MscrClientImpl();
        MscrSchema sourceSchema = null;
        try {
            sourceSchema = client.searchSchema("TEI minimal");
        } catch(MscrNotFoundException ex) {
            throw new Exception("Source schema not found");
        }
        
        MscrSchema targetSchema = null;
        try {
            targetSchema = client.searchSchema("CLARIN Dublin Core");
        } catch(MscrNotFoundException ex) {
            throw new Exception("Target schema not found");
        }
        
        logger.info("Source schema id: "+sourceSchema.source.id+", target schema id: "+targetSchema.source.id);
        
        MscrCrosswalk crosswalk = null;
        try {
            crosswalk = client.searchCrosswalk(sourceSchema.source.id, targetSchema.source.id);
        } catch(MscrNotFoundException ex) {
            throw new Exception("Crosswalk not found, source schema: "+sourceSchema.source.id+", target schema: "+targetSchema.source.id);
        }
        
        logger.info("Crosswalk id: "+crosswalk.source.id);
        
        //MscrCrosswalkMetadata crosswalkMetadata = null;
                
        MscrCrosswalkMetadata crosswalkMetadata = client.fetchCrosswalkMetadata(crosswalk.source.id);
        logger.info(""+crosswalkMetadata.format);
        
        if(crosswalkMetadata.files.length > 0) {
            String xslt = client.fetchCrosswalkXslt(crosswalk.source.id, crosswalkMetadata.files[0].fileID);
            
            //TEI file
            String url = "https://llds.ling-phil.ox.ac.uk/llds/xmlui/bitstream/handle/20.500.14106/A39119/A39119.xml?sequence=7&isAllowed=y";
        
            String transformedXml = client.transform(url, xslt);
            logger.info(""+transformedXml);
        }
    }
    
    //@Test
    public void testParse() throws Exception {
        String xmlMimeType = "text/xml";
        URL res1 = getClass().getResource("/tei_example.xml");
        
        String xmlContent = new BufferedReader(
            new InputStreamReader(res1.openStream(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n")
        );
        
        MscrReferenceParser parser = new MscrReferenceParser();
        boolean result = parser.parse(xmlContent, xmlMimeType);
        logger.info("Parsing result: "+result);
    }
}
