package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  {
 *      "type":null,
 *      "status":"DRAFT",
 *      "state":"PUBLISHED",
 *      "versionLabel":"1",
 *      "visibility":"PUBLIC",
 * 
 *      "created":"2024-11-11T08:52:09.729Z",
 *      "modified":"2024-11-11T08:52:09.729Z",
 *      "modifier":{
 *          "id":"40438e38-ec5c-4606-9075-a2c29b016126",
 *          "name":"Willem Elbers"
 *      },
 *      "creator":{
 *          "id":"40438e38-ec5c-4606-9075-a2c29b016126",
 *          "name":"Willem Elbers"
 *      },
 *      "prefix":null,
 *      "label":{"en":"TEI minimal 2"},
 *      "description":{},
 *      "languages":["en"],
 *      "organizations":[],
 *      "groups":[],
 *      "internalNamespaces":[],
 *      "externalNamespaces":[],
 *      "terminologies":[],
 *      "codeLists":[],
 *      "contact":null,
 *      "documentation":{},
 *      "handle":"21.T13999/EOSC-202411000283352",
 *      "fileMetadata":[],
 *      "format":"XSD",
 *      "namespace":"http://test2.com",
 *      "revisionOf":null,
 *      "aggregationKey":"mscr:schema:76d163d7-8e62-49d0-b273-0874f8eb068a",
 *      "hasRevisions":null,
 *      "revisions":null,
 *      "variants":null,
 *      "variants2":null,
 *      "owner":["40438e38-ec5c-4606-9075-a2c29b016126"],
 *      "ownerMetadata":[{"id":"40438e38-ec5c-4606-9075-a2c29b016126","name":"Willem Elbers"}],
 *      "sourceURL":null,
 *      "customRoot":null,
 *      "pid":"mscr:schema:76d163d7-8e62-49d0-b273-0874f8eb068a"
 *  }
 * @author wilelb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MscrSchemaUploadResponse extends MscrBasicFields {
    @JsonProperty(value = "handle")
    public String handle;
    @JsonProperty(value = "pid")
    public String pid;
}
