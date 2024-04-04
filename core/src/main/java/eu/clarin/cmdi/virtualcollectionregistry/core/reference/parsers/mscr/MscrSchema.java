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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;


/**
 * 
     {
        "_index": "schemas_v2",
        "_id": "urn%3AIAMNOTAPID%3Ac75541d2-e275-4a7f-8ab4-b94653da265d",
        "_source": {
          "id": "urn:IAMNOTAPID:c75541d2-e275-4a7f-8ab4-b94653da265d",
          "label": {
            "en": "TEI minimal"
          },
          "status": "DRAFT",
          "modified": "2024-03-05T11:16:44.851Z",
          "created": "2024-03-05T11:16:44.851Z",
          "contentModified": "2024-03-05T11:16:44.851Z",
          "type": "SCHEMA",
          "state": "DRAFT",
          "visibility": "PUBLIC",
          "prefix": "urn:IAMNOTAPID:c75541d2-e275-4a7f-8ab4-b94653da265d",
          "comment": {
            "en": "https://tei-c.org/release/xml/tei/custom/schema/xsd/tei_minimal.xsd"
          },
          "contributor": [],
          "organizations": [],
          "isPartOf": [],
          "language": [
            "en"
          ],
          "format": "XSD",
          "aggregationKey": "urn:IAMNOTAPID:c75541d2-e275-4a7f-8ab4-b94653da265d",
          "numberOfRevisions": 1,
          "revisions": [
            {
              "pid": "urn:IAMNOTAPID:c75541d2-e275-4a7f-8ab4-b94653da265d",
              "created": 1709637404851,
              "label": {
                "en": "TEI minimal"
              },
              "versionLabel": "1",
              "state": "DRAFT"
            }
          ],
          "owner": [
            "3b0cfd7b-b4ce-41be-aa9e-ac28bb3148ed"
          ],
          "versionLabel": "1",
          "namespace": "http://test.com"
        },
        "sort": [
          null
        ]
      }
    
 */


/**
 *
 * @author wilelb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MscrSchema {
    //sort property
    @JsonProperty(value = "_index")
    public String index;
    @JsonProperty(value = "_id")
    public String id;
    @JsonProperty(value = "_source")
    public Source source;
    
    /*
    @JsonProperty(value = "_index")
    public String index;
    @JsonProperty(value = "_id")
    public String id;
    @JsonProperty(value = "format")
    public String format;
    @JsonProperty(value = "versionLabel")
    public String versionLabel;
*/
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source extends MscrBasicFields {
        @JsonProperty(value = "id")
        public String id;
        
        @JsonProperty(value = "label")
        public HashMap<String, String> label;
                
        @JsonProperty(value = "namespace")
        public String namespace;
        
        public String getLabel(String key) {
            if(label.containsKey(key)) {
                return label.get(key);
            }
            return "";
        }
    }
}
