package eu.clarin.cmdi.mscr.client.lib;

/**
 *
 * @author wilelb
 */
public class MscrApiConfiguration {

    private final static String DEFAULT_BASE_URL = "https://mscr-test.2.rahtiapp.fi/datamodel-api/v2/";
    
    private final String apiBaseUrl;
    private final String apiKey;
    
    public MscrApiConfiguration(String apiKey) {
        this(DEFAULT_BASE_URL, apiKey);
    }
    
    public MscrApiConfiguration(String baseUrl, String apiKey) {
        this.apiBaseUrl = baseUrl;
        this.apiKey = apiKey;
    }
    
    public String getServiceBaseURL() {
        return this.apiBaseUrl;
    }
    
    public String getApiKey() {
        return this.apiKey;
    }
}
