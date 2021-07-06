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
        return String.format("%s/service/virtualcollections/%d", baseUri, collectionId);
    }
}
