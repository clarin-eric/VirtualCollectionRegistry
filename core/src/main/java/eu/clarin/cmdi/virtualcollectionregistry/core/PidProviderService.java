package eu.clarin.cmdi.virtualcollectionregistry.core;

import eu.clarin.cmdi.virtualcollectionregistry.core.pid.PersistentIdentifierProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;
import java.util.List;

public interface PidProviderService {
    public List<PersistentIdentifierProvider> getProviders();
    public List<PersistentIdentifier> createIdentifiers(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException;
    public List<PersistentIdentifier> createLatestIdentifiers(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException;

    public void updateLatestIdentifierUrl(PersistentIdentifier pid, String newUrl) throws VirtualCollectionRegistryException;
    public void updateIdentifierUrl(PersistentIdentifier pid, String newUrl) throws VirtualCollectionRegistryException;
}
