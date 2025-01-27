package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

/**
 *  {
 *      "took":2,
 *      "timed_out":false,
 *      "_shards":{
 *          "failed":0.0,
 *          "successful":2.0,
 *          "total":2.0,
 *          "skipped":0.0
 *      },
 *      "hits":{
 *          "total":{
 *              "relation":"eq",
 *              "value":3
 *          },"hits":[
 *              {
 *                  "_index":"crosswalks_v2",
 *                  "_id":"mscr%3Acrosswalk%3A9286957f-f837-4db1-a93a-47fbb724c8db",
 *                  "_source":{
 *                      "id":"mscr:crosswalk:9286957f-f837-4db1-a93a-47fbb724c8db",
 *                      "label":{"en":"tei minimal to dc"},
 *                      "status":"VALID",
 *                      "modified":"2024-10-10T07:41:16.776Z",
 *                      "created":"2024-10-10T07:41:16.776Z",
 *                      "contentModified":"2024-10-10T07:41:16.776Z",
 *                      "type":"CROSSWALK",
 *                      "state":"PUBLISHED",
 *                      "visibility":"PUBLIC",
 *                      "prefix":"mscr:crosswalk:9286957f-f837-4db1-a93a-47fbb724c8db",
 *                      "comment":{},
 *                      "contributor":[],
 *                      "organizations":[],
 *                      "isPartOf":[],
 *                      "language":["en"],
 *                      "aggregationKey":"mscr:crosswalk:9286957f-f837-4db1-a93a-47fbb724c8db",
 *                      "hasRevision":"false",
 *                      "numberOfRevisions":1,
 *                      "owner":["40438e38-ec5c-4606-9075-a2c29b016126"],
 *                      "versionLabel":"1",
 *                      "handle":"21.T13999/EOSC-202410000279545",
 *                      "sourceSchema":"mscr:schema:76fa81ab-efd6-44a9-a3e0-6af6cd2e7f92",
 *                      "targetSchema":"mscr:schema:83c910ba-3896-4423-b2d2-bdbfc50b7a7e"
 *                  },
 *                  "sort":[null]
 *              }, {
 *                  "_index":"crosswalks_v2","_id":"mscr%3Acrosswalk%3Af9497952-af2f-4840-8cad-1da7ba0528f7","_source":{"id":"mscr:crosswalk:f9497952-af2f-4840-8cad-1da7ba0528f7","label":{"en":"tei minimal to dc"},"status":"VALID","modified":"2024-11-15T12:04:32.073Z","created":"2024-11-15T12:04:32.073Z","contentModified":"2024-11-15T12:04:32.073Z","type":"CROSSWALK","state":"PUBLISHED","visibility":"PUBLIC","prefix":"mscr:crosswalk:f9497952-af2f-4840-8cad-1da7ba0528f7","comment":{},"contributor":[],"organizations":[],"isPartOf":[],"language":["en"],"format":"MSCR","aggregationKey":"mscr:crosswalk:f9497952-af2f-4840-8cad-1da7ba0528f7","hasRevision":"false","numberOfRevisions":1,"owner":["40438e38-ec5c-4606-9075-a2c29b016126"],"versionLabel":"1","handle":"21.T13999/EOSC-202411000283913","sourceSchema":"21.T13999/EOSC-202411000283272","targetSchema":"21.T13999/EOSC-202411000283352"},"sort":[null]
 *              },{
 *                  "_index":"crosswalks_v2","_id":"mscr%3Acrosswalk%3A2bd18afe-3f42-49a2-94e1-a5119df37e0a","_source":{"id":"mscr:crosswalk:2bd18afe-3f42-49a2-94e1-a5119df37e0a","label":{"en":"tei minimal to dc"},"status":"VALID","modified":"2024-11-15T12:30:54.595Z","created":"2024-11-15T12:30:54.595Z","contentModified":"2024-11-15T12:30:54.595Z","type":"CROSSWALK","state":"PUBLISHED","visibility":"PUBLIC","prefix":"mscr:crosswalk:2bd18afe-3f42-49a2-94e1-a5119df37e0a","comment":{},"contributor":[],"organizations":[],"isPartOf":[],"language":["en"],"format":"MSCR","aggregationKey":"mscr:crosswalk:2bd18afe-3f42-49a2-94e1-a5119df37e0a","hasRevision":"false","numberOfRevisions":1,"owner":["40438e38-ec5c-4606-9075-a2c29b016126"],"versionLabel":"1","handle":"21.T13999/EOSC-202411000283922","sourceSchema":"21.T13999/EOSC-202411000283272","targetSchema":"21.T13999/EOSC-202411000283352"},"sort":[null]
 *              }
 *          ]
 *      }
 *  }
 * @author wilelb
 */
public class MscrCrosswalk {
    //sort property
    @JsonProperty(value = "_index")
    public String index;
    @JsonProperty(value = "_id")
    public String id;
    @JsonProperty(value = "_source")
    public Source source;
    
    @JsonIgnoreProperties(ignoreUnknown = true)
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
