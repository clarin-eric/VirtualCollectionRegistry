package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PermaLinkServiceImpl implements PermaLinkService {
    @Value("${eu.clarin.cmdi.virtualcollectionregistry.base_uri}")
    private String baseUri;

    public PermaLinkServiceImpl() {}

    public PermaLinkServiceImpl(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public String getBaseUri() {
        return baseUri;
    }

    @Override
    public String getCollectionUrl(VirtualCollection collection) {
        if(collection == null) {
            throw new RuntimeException("collection cannot be null");
        }
        return getCollectionUrl(collection.getId());
    }

    @Override
    public String getCollectionUrl(Long collectionId) {
        if(baseUri == null) {
            throw new RuntimeException("baseUri cannot be null");
        }
        if(baseUri.endsWith("/")) {
            baseUri = baseUri.substring(0, baseUri.length()-1);
        }
        if(collectionId == null) {
            throw new RuntimeException("collectionId cannot be null");
        }
        return String.format("%s/service/virtualcollections/%d", baseUri, collectionId);
    }

    @Override
    public String getCollectionDetailsUrl(VirtualCollection collection) {
        if(collection == null) {
            throw new RuntimeException("collection cannot be null");
        }
        return getCollectionDetailsUrl(collection.getId());
    }

    @Override
    public String getCollectionDetailsUrl(Long collectionId) {
        if(baseUri == null) {
            throw new RuntimeException("baseUri cannot be null");
        }
        if(baseUri.endsWith("/")) {
            baseUri = baseUri.substring(0, baseUri.length()-1);
        }
        if(collectionId == null) {
            throw new RuntimeException("collectionId cannot be null");
        }
        return String.format("%s/details/%d", baseUri, collectionId);
    }
}
