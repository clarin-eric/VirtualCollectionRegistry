package eu.clarin.cmdi.mscr.client.lib;

import java.util.List;
import java.util.Map;

/**
 *  {
 *      "namespace":"http://test.com",
 *      "description":{},
 *      "label":{"en":"TEI minimal"},
 *      "languages":["en"],
 *      "organizations":[],
 *      "format":"XSD",
 *      "status":"DRAFT",
 *      "state":"PUBLISHED",
 *      "versionLabel":"1"
 *  }
 * @author wilelb
 */
public class MscrSchemaUploadRequest {
    
    public static MscrSchemaUploadRequestBuilder builder() {
        return new MscrSchemaUploadRequestBuilder();
    }          
    
    public Map<String, String> description;
    public Map<String, String> label;
    public List<String> languages;
    public List<String> organizations;    
    public String status;
    public String state;
    public String versionLabel;
    public String visibility;
    
    public String format;
    public String namespace;
}
