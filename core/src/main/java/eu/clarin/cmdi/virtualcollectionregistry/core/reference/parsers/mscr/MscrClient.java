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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author wilelb
 */
public interface MscrClient {
    public String getCrosswalkIdFromUrl(String url);
    
    /**
     * Search a schema in the MSCR registry.
     * @param query
     * @return The first search result
     * @throws eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrNotFoundException
     * @throws java.io.IOException
     */
    public MscrSchema searchSchema(String query) throws MscrNotFoundException, IOException;
    /**
     * Search for schemas in the MSCR registry.
     * @param query
     * @return A list of schemas matching the query.
     * @throws eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrNotFoundException
     * @throws java.io.IOException
     */
    public List<MscrSchema> searchSchemas(String query) throws MscrNotFoundException, IOException;
    
    /**
     * Search a crosswalk in the MSCR registry.
     * @param sourceSchemaId
     * @param targetSchemaId
     * @return The first search result.
     * @throws eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrNotFoundException
     * @throws java.io.IOException
     */
    public MscrCrosswalk searchCrosswalk(String sourceSchemaId, String targetSchemaId)throws MscrNotFoundException, IOException;
    /**
     * Search for crosswalks in the MSCR registry.
     * @param sourceSchemaId
     * @param targetSchemaId
     * @return A list of crosswalks matching the query.
     * @throws eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr.MscrNotFoundException
     * @throws java.io.IOException
     */
    public List<MscrCrosswalk> searchCrosswalks(String sourceSchemaId, String targetSchemaId) throws MscrNotFoundException, IOException;
    
    /**
     * 
     * @param crosswalkId
     * @return
     * @throws MscrNotFoundException 
     * @throws java.io.IOException 
     */
    public MscrCrosswalkMetadata fetchCrosswalkMetadata(String crosswalkId) throws MscrNotFoundException, IOException;
    
    /**
     * 
     * @param crosswalkId
     * @param fileId
     * @return 
     * @throws MscrNotFoundException 
     * @throws java.io.IOException 
     */
    public String fetchCrosswalkXslt(String crosswalkId, String fileId) throws MscrNotFoundException, IOException;
    
    //public String getUrlContent(String url) throws IOException;
    public String transformFromUrl(String url, String xslt) throws IOException;
    public String transform(String xmlContent, String xsltContent) ;
    
    public String getNamespaceUriFromXml(String xmlContent) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException;
}
