package eu.clarin.cmdi.virtualcollectionregistry.core.apikey;


import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;

public interface ApiKeyService {
    public void generateNewKeyForUser(String username);
    public void revokeKey(String key);
    User getUserForApiKey(String key) throws ApiKeyNotFoundException, ApiKeyRevokedException, ApiKeyException;
}
