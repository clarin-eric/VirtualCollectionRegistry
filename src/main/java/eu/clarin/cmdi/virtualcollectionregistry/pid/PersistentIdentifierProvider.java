package eu.clarin.cmdi.virtualcollectionregistry.pid;

import java.net.URI;

import eu.clarin.cmdi.virtualcollectionregistry.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

public abstract interface PersistentIdentifierProvider {

    String getId();

    PersistentIdentifier createIdentifier(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException;

    void updateIdentifier(String pid, URI target) throws VirtualCollectionRegistryException;

    void deleteIdentifier(String pid) throws VirtualCollectionRegistryException;

    boolean ownsIdentifier(String pid);

    boolean isPrimaryProvider();

    void setPrimaryProvider(boolean primary);

    String getInfix();

    PublicConfiguration getPublicConfiguration();
}
