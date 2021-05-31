package eu.clarin.cmdi.virtualcollectionregistry;

public class ApiKeyRevokedException extends ApiKeyException {
    public ApiKeyRevokedException(String key) {
        super("Api key ("+key+") is revoked");
    }
}
