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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLDateBuilder;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import de.undercouch.citeproc.csl.CSLType;
import eu.clarin.cmdi.virtualcollectionregistry.model.citation.Citable;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 * @author wilelb
 */
public class CslJsonGenerator {
    private final static Logger logger = LoggerFactory.getLogger(CslJsonGenerator.class);
    
    private final static ObjectMapper mapper = new ObjectMapper();

    public abstract class CSLItemDataMixin {
        @JsonProperty("DOI") abstract String getDOI();
        @JsonProperty("ISBN") abstract String getISBN();
        @JsonProperty("ISSN") abstract String getISSN();
        @JsonProperty("URL") abstract String getURL();
    }

    public abstract class CSLDateMixin {
        @JsonProperty("date-parts") abstract int[][] getDateParts();
    }

    public static String generateCslJson(Citable c) {
        CSLDate issuedDate = new CSLDateBuilder().dateParts(2008, 2).build();

        List<String> keywords = c.getKeywords();
        
        CSLItemDataBuilder builder = new CSLItemDataBuilder()
            .id(c.getId().toString())
            .DOI(c.getIdentifier())
            .URL(c.getUri())
            .type(CSLType.COLLECTION)
            .title(c.getTitle())               
            .issued(issuedDate)
            .publisher("Virtual Collection Registry")
            .language("en")
            .categories(keywords.toArray(new String[0]));
        
        List<CSLName> authorList = new ArrayList<>();
        for(Creator author : c.getAuthors()) {
            authorList.add(new CSLNameBuilder()
                .given(author.getGivenName())
                .family(author.getFamilyName())
                .build());
        }
        builder = builder.author(authorList.toArray(new CSLName[0]));
        
        try {
            /*
            String json = 
                mapper
                    .copy()
                    .addMixIn(CSLItemData.class, CSLItemDataMixin.class)
                    .addMixIn(CSLDate.class, CSLDateMixin.class)
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(builder.build());
            logger.info("CSL: {}", json);
            */
            
            return mapper
                    .copy()
                    .addMixIn(CSLItemData.class, CSLItemDataMixin.class)
                    .addMixIn(CSLDate.class, CSLDateMixin.class)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(builder.build());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to generate CSL json", ex);
        }
        
        return "";
    }
}
