/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.wicket.components.citation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;
import eu.clarin.cmdi.virtualcollectionregistry.model.citation.Citable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class CslJsonGenerator {
    private final static Logger logger = LoggerFactory.getLogger(CslJsonGenerator.class);
    
    private final static ObjectMapper mapper = new ObjectMapper();
    
    public static String generateCslJson(Citable c) {
        CSLItemDataBuilder builder = new CSLItemDataBuilder()
            .id(c.getId().toString())
            .type(CSLType.COLLECTION)
            .title(c.getTitle())   
            .URL(c.getUri())
            .issued(2008)
            .publisher("CLARIN Virtual Collection Registry");

        builder.DOI(c.getIdentifier());

        try {
            return mapper
                    .copy()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL) //Omit keys for null values
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(builder.build());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to generate CSL json", ex);
        }
        
        return "";
    }
}
