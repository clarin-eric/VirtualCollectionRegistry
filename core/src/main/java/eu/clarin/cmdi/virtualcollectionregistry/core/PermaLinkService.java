package eu.clarin.cmdi.virtualcollectionregistry.core;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;


public interface PermaLinkService {
    String getBaseUri();
    String getCollectionUrl(VirtualCollection collection);
    String getCollectionUrl(Long collectionId);
    String getCollectionDetailsUrl(VirtualCollection collection);
    String getCollectionDetailsUrl(Long collectionId);
}
