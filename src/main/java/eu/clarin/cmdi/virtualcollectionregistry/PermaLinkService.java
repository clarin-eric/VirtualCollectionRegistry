package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

public interface PermaLinkService {
    String getBaseUri();
    String getCollectionUrl(VirtualCollection collection);
    String getCollectionUrl(Long collectionId);
    String getCollectionDetailsUrl(VirtualCollection collection);
    String getCollectionDetailsUrl(Long collectionId);
}
