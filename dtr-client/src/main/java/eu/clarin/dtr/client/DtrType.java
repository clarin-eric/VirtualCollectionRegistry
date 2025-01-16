/*
 * Copyright (C) 2025 CLARIN
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
package eu.clarin.dtr.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * {
  "Identifier": "21.T11969/710b1a3647d431e205e0",
  "name": "application/tei+xml",
  "description": "Text Encoding and Interchange (TEI) is an international and interdisciplinary standard that is widely used by libraries, museums, publishers, and individual scholars to represent all kinds of textual material for online research and teaching",
  "provenance": {
    "contributors": [
      {
        "Name": "Willem Elbers"
      }
    ],
    "creationDate": "2024-11-25T10:34:29.283Z",
    "lastModificationDate": "2024-11-29T07:58:15.914Z"
  },
  "Aliases": [
    "application/tei+xml"
  ],
  "Taxonomies": [
    "21.T11969/fe61e4792b37f2bbb26e"
  ],
  "typeName": "application",
  "subTypeName": "tei+xml",
  "references": [
    {
      "referenceName": "RFC",
      "referenceURL": "https://datatracker.ietf.org/doc/html/rfc6129"
    },
    {
      "referenceName": "SCHEMA",
      "referenceURL": "https://tei-c.org/release/xml/tei/custom/schema/xsd/tei_lite_xml.xsd"
    },
    {
      "referenceName": "CROSSWALK",
      "referenceURL": "https://mscr-test.2.rahtiapp.fi/datamodel-api/v2/crosswalk/21.T13999/EOSC-202411000283922"
    }
  ]
}
* 
 * @author wilelb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtrType {
    @JsonProperty(value = "Identifier")
    public String identifier;
    
    @JsonProperty(value = "name")
    public String name;
    
    @JsonProperty(value = "description")
    public String description;
    
    @JsonProperty(value = "Aliases")
    public String[] aliases;
    
    @JsonProperty(value = "Taxonomies")
    public String[] taxonomies;
    
    @JsonProperty(value = "typeName")
    public String typeName;
    
    @JsonProperty(value = "subTypeName")
    public String subTypeName;
    
    @JsonProperty(value = "references")
    public DtrReference[] references;
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DtrReference {
        public DtrReference() {}
        
        public DtrReference(String name, String url) {
            this.name = name;
            this.url = url;
        }
        
        @JsonProperty(value = "referenceName")
        public String name;
        @JsonProperty(value = "referenceURL")
        public String url;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class DtrProvenance {
        @JsonProperty(value = "creationDate")
        public String creationDate;
        
        @JsonProperty(value = "lastModificationDate")
        public String lastModificationDate;
        
        @JsonProperty(value = "contributors")
        public DtrContributor[] contributors;
    }
    
    public class DtrContributor {
        @JsonProperty(value = "name")
        public String name;
    
    }
}
