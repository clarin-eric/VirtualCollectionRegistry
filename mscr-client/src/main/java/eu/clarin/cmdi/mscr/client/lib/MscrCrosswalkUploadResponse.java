package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {
 * "created":"2024-11-15T12:30:54.595Z",
 * "modified":"2024-11-15T12:30:54.595Z",
 * "modifier":{"id":"40438e38-ec5c-4606-9075-a2c29b016126","name":"Willem Elbers"},
 * "creator":{"id":"40438e38-ec5c-4606-9075-a2c29b016126","name":"Willem Elbers"},
 * "type":null,
 * "prefix":null,
 * "status":"VALID",
 * "label":{"en":"tei minimal to dc"},
 * "description":{},
 * 
 * "languages":["en"],
 * "organizations":[],
 * "groups":[],
 * "internalNamespaces":[],
 * "externalNamespaces":[],
 * "terminologies":[],
 * "codeLists":[],
 * "contact":null,
 * "documentation":{},
 * "handle":"21.T13999/EOSC-202411000283922",
 * "state":"PUBLISHED",
 * "visibility":"PUBLIC",
 * "format":"MSCR",
 * "aggregationKey":"mscr:crosswalk:2bd18afe-3f42-49a2-94e1-a5119df37e0a",
 * "fileMetadata":[{"contentType":"application/octet-stream","size":997,"fileID":146,"filename":"mscr:crosswalk:2bd18afe-3f42-49a2-94e1-a5119df37e0a.octet-stream","timestamp":"2024-11-320T12:11:777+0000"}],
 * "sourceSchema":"http://mscr-fuseki.mscr-test.svc:3030/crosswalk/21.T13999/EOSC-202411000283272",
 * "targetSchema":"http://mscr-fuseki.mscr-test.svc:3030/crosswalk/21.T13999/EOSC-202411000283352",
 * "owner":["40438e38-ec5c-4606-9075-a2c29b016126"],
 * "mappings":null,
 * "versionLabel":"1",
 *      "revisionOf":null,
 *      "hasRevisions":null,
 *      "revisions":null,
 *      "ownerMetadata":[{"id":"40438e38-ec5c-4606-9075-a2c29b016126","name":"Willem Elbers"}],
 *      "sourceURL":null,
 *      "pid":"mscr:crosswalk:2bd18afe-3f42-49a2-94e1-a5119df37e0a"
 *  }
 * @author wilelb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MscrCrosswalkUploadResponse {
    @JsonProperty(value = "handle")
    public String handle;
    @JsonProperty(value = "pid")
    public String pid;
}
