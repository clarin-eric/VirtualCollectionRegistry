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
  {
    "_index": "crosswalks_v2",
    "_id": "urn%3AIAMNOTAPID%3A07deee14-a329-4a34-9885-f6e897b9d806",
    "_source": {
      "id": "urn:IAMNOTAPID:07deee14-a329-4a34-9885-f6e897b9d806",
      "label": {
        "en": "Test v2"
      },
      "status": "VALID",
      "modified": "2024-03-05T07:37:57.889Z",
      "created": "2024-03-05T07:37:57.889Z",
      "contentModified": "2024-03-05T07:37:57.889Z",
      "type": "CROSSWALK",
      "state": "DRAFT",
      "visibility": "PUBLIC",
      "prefix": "urn:IAMNOTAPID:07deee14-a329-4a34-9885-f6e897b9d806",
      "comment": {},
      "contributor": [],
      "organizations": [],
      "isPartOf": [],
      "language": [
        "en"
      ],
      "numberOfRevisions": 0,
      "owner": [
        "3b0cfd7b-b4ce-41be-aa9e-ac28bb3148ed"
      ],
      "versionLabel": "1",
      "sourceSchema": "urn:IAMNOTAPID:4122d383-916d-4c2b-85e8-3acb9d32d644",
      "targetSchema": "urn:IAMNOTAPID:675ef6a9-189b-405f-8c20-a6297b2eb26d"
    },
    "sort": [
      null
    ]
  }
 * @author wilelb
 */


@JsonIgnoreProperties(ignoreUnknown = true)
public class MscrCrosswalk {
    //sort property
    @JsonProperty(value = "_index")
    public String index;
    @JsonProperty(value = "_id")
    public String id;
    @JsonProperty(value = "_source")
    public Source source;

    public static class Source extends MscrBasicFields {
        @JsonProperty(value = "id")
        public String id;
        @JsonProperty(value = "sourceSchema")
        public String sourceSchema;
        @JsonProperty(value = "targetSchema")
        public String targetSchema;
        
        @JsonProperty(value = "label")
        public HashMap<String, String> label;
    }
}
