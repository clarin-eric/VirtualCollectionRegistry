package eu.clarin.cmdi.mscr.client.lib;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author wilelb
 */
public class MscrCrosswalkUploadRequestBuilder extends RequestBuilder {
    
    private final MscrCrosswalkUploadRequest request = new MscrCrosswalkUploadRequest();
    
    public MscrCrosswalkUploadRequestBuilder() {
        request.status = "VALID";
        request.state = "PUBLISHED";
        request.versionLabel = "1";        
        request.description = new HashMap<>();
        request.label = new HashMap<>();
        request.languages = new ArrayList<>();
        request.organizations = new ArrayList<>();
        
        request.visibility = "PUBLIC";
        request.format = "MSCR";           
    }
    
    public MscrCrosswalkUploadRequest build() {
        return request;
    }
    
    public MscrCrosswalkUploadRequestBuilder setFormat(MscrCrosswalkUploadRequest.CrosswalkFormat format) {
        request.format = format.toString();
        return this;
    }
    
    public MscrCrosswalkUploadRequestBuilder setStatus(String status) {
        request.status = status;
        return this;
    }
    
    public MscrCrosswalkUploadRequestBuilder setState(String visibility) {
        request.state = visibility;
        return this;
    }
    
    public MscrCrosswalkUploadRequestBuilder addLabel(String lang, String label) {
        request.label.put(lang, label);
        return this;
    }

    public MscrCrosswalkUploadRequestBuilder addDescription(String lang, String description) {
        request.description.put(lang, description);
        return this;
    }

    public MscrCrosswalkUploadRequestBuilder addLanguage(String language) {            
        if(!exists(request.languages, language)) {
            request.languages.add(language);
        }
        return this;
    }

    public MscrCrosswalkUploadRequestBuilder addOrganizations(String organization) {
        if(!exists(request.organizations, organization)) {
            request.organizations.add(organization);
        }
        return this;
    }
    
    public MscrCrosswalkUploadRequestBuilder setSourceSchema(String sourceSchema) {
        request.sourceSchema = sourceSchema;
        return this;
    }
    
    public MscrCrosswalkUploadRequestBuilder setTargetSchema(String targetSchema) {
        request.targetSchema = targetSchema;
        return this;
    }
    
    public MscrCrosswalkUploadRequestBuilder setFormat(String format) {
        request.format = format;
        return this;
    }
    
    public MscrCrosswalkUploadRequestBuilder setVisibility(String visibility) {
        request.visibility = visibility;
        return this;
    }
}
