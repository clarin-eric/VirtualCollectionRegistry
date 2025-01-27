package eu.clarin.cmdi.mscr.client.lib;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author wilelb
 */
public class MscrSchemaUploadRequestBuilder extends RequestBuilder {
    private final MscrSchemaUploadRequest uploadMetadata = new MscrSchemaUploadRequest();
        
    public MscrSchemaUploadRequestBuilder() {
        uploadMetadata.format = "XSD";
        uploadMetadata.state = "DRAFT";
        uploadMetadata.status = "DRAFT";
        uploadMetadata.visibility = "PRIVATE";
        uploadMetadata.versionLabel = "1";
        uploadMetadata.description = new HashMap<>();
        uploadMetadata.label = new HashMap<>();
        uploadMetadata.languages = new ArrayList<>();
        uploadMetadata.organizations = new ArrayList<>();
    }

    public MscrSchemaUploadRequest build() {
        return uploadMetadata;
    }       

    public MscrSchemaUploadRequestBuilder setState(String state) {
        uploadMetadata.state = state;
        return this;
    }
    /*
    public MscrSchemaUploadRequestBuilder setState(String visibility) {
        uploadMetadata.state = visibility;
        return this;
    }
    */
    
    public MscrSchemaUploadRequestBuilder setVisibility(String visibility) {
        uploadMetadata.visibility = visibility;
        return this;
    }
    
    public MscrSchemaUploadRequestBuilder addNamespace(String namespace) {
        uploadMetadata.namespace = namespace;
        return this;
    }

    public MscrSchemaUploadRequestBuilder addLabel(String lang, String label) {
        uploadMetadata.label.put(lang, label);
        return this;
    }

    public MscrSchemaUploadRequestBuilder addDescription(String lang, String description) {
        uploadMetadata.description.put(lang, description);
        return this;
    }

    public MscrSchemaUploadRequestBuilder addLanguage(String language) {            
        if(!exists(uploadMetadata.languages, language)) {
            uploadMetadata.languages.add(language);
        }
        return this;
    }

    public MscrSchemaUploadRequestBuilder addOrganizations(String organization) {
        if(!exists(uploadMetadata.organizations, organization)) {
            uploadMetadata.organizations.add(organization);
        }
        return this;
    }
}
