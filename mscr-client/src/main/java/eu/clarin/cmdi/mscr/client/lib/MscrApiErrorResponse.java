package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  {
 *      "type":"about:blank",
 *      "title":"Bad Request",
 *      "status":400,
 *      "detail":"Invalid state change. Allowed transitions: PUBLISHED -> INVALID, PUBLISHED -> DEPRECATED",
 *      "instance":"/datamodel-api/v2/schema/21.T13999/EOSC-202411000283272"
 *  }
 * 
 * @author wilelb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MscrApiErrorResponse {
    @JsonProperty(value = "type")
    public String type;
    @JsonProperty(value = "title")
    public String title;
    @JsonProperty(value = "status")
    public int status;
    @JsonProperty(value = "detail")
    public String detail;
    @JsonProperty(value = "instance")
    public String instance;
}
