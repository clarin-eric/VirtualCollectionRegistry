package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.ApiKey;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;

import java.security.Principal;
import java.util.Set;

public interface ApiKeyService {
    public void generateNewKeyForUser(String username);
    public void revokeKey(String key);
    User getUserForApiKey(String key) throws ApiKeyNotFoundException, ApiKeyRevokedException, ApiKeyException;
}
