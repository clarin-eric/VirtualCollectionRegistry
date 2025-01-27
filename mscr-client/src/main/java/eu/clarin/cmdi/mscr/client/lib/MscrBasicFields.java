package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author wilelb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MscrBasicFields {
    
    @JsonProperty(value = "status")
    public String status;
    @JsonProperty(value = "type")
    public String type;
    @JsonProperty(value = "state")
    public String state;
    @JsonProperty(value = "visibility")
    public String visibility;
    @JsonProperty(value = "versionLabel")
    public String versionLabel;
}