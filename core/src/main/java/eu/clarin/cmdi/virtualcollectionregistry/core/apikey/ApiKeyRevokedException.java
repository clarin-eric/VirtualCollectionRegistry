package eu.clarin.cmdi.virtualcollectionregistry.core.apikey;

public class ApiKeyRevokedException extends ApiKeyException {
    public ApiKeyRevokedException(String key) {
        super("Api key ("+key+") is revoked");
    }
}
