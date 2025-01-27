package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

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
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source extends MscrBasicFields {
        @JsonProperty(value = "id")
        public String id;
        
        @JsonProperty(value = "handle")
        public String handle;
        
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
