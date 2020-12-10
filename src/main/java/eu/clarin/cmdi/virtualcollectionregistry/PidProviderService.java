package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifierProvider;

import java.util.List;

public interface PidProviderService {
    public List<PersistentIdentifierProvider> getProviders();
    public List<PersistentIdentifier> createIdentifiers(VirtualCollection vc) throws VirtualCollectionRegistryException;
}
