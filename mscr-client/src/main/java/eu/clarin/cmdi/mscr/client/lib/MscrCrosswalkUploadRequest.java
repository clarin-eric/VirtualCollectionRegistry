package eu.clarin.cmdi.mscr.client.lib;

import java.util.List;
import java.util.Map;

/**
 *  {
 *      "format":"MSCR",
 *      "description":{},
 *      "label":{"en":"tei minimal to dc"},
 *      "languages":["en"],
 *      "status":"VALID",
 *      "state":"PUBLISHED",
 *      "sourceSchema":"mscr:schema:76fa81ab-efd6-44a9-a3e0-6af6cd2e7f92",
 *      "targetSchema":"mscr:schema:83c910ba-3896-4423-b2d2-bdbfc50b7a7e",
 *      "versionLabel":"1",
 *      "visibility":"PUBLIC",
 *      "organizations":[]
 *  }
 * @author wilelb
 */
public class MscrCrosswalkUploadRequest {
    
    public enum CrosswalkFormat {
            XSLT
    }
    
    public static MscrCrosswalkUploadRequestBuilder builder() {
        return new MscrCrosswalkUploadRequestBuilder();
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
    public String sourceSchema;
    public String targetSchema; 
}
