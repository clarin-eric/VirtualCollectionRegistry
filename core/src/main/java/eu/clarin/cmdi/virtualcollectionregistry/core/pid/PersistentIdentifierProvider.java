package eu.clarin.cmdi.virtualcollectionregistry.core.pid;

import java.net.URI;

import eu.clarin.cmdi.virtualcollectionregistry.core.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;

public interface PersistentIdentifierProvider {

    String getId();

    PersistentIdentifier createIdentifier(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException;

    PersistentIdentifier createIdentifier(VirtualCollection vc, String suffix, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException;

    void updateIdentifier(PersistentIdentifier pid, URI target) throws VirtualCollectionRegistryException;

    void deleteIdentifier(String pid) throws VirtualCollectionRegistryException;

    boolean ownsIdentifier(String pid);

    boolean isPrimaryProvider();

    void setPrimaryProvider(boolean primary);

    String getInfix();

    PublicConfiguration getPublicConfiguration();
}
