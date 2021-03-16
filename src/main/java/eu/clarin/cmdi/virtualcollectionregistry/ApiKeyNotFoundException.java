package eu.clarin.cmdi.virtualcollectionregistry;

public class ApiKeyNotFoundException extends ApiKeyException {
    public ApiKeyNotFoundException(String key) {
        super("Api key ("+key+") not found");
    }
}
