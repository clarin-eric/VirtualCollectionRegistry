package eu.clarin.cmdi.virtualcollectionregistry.core.apikey;

public class ApiKeyNotFoundException extends ApiKeyException {
    public ApiKeyNotFoundException(String key) {
        super("Api key ("+key+") not found");
    }
}
