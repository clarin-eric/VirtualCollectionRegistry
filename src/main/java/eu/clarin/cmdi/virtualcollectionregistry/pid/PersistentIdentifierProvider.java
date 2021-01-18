package eu.clarin.cmdi.virtualcollectionregistry.pid;

import java.net.URI;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

public abstract interface PersistentIdentifierProvider {

    String getId();
    
    PersistentIdentifier createIdentifier(VirtualCollection vc) throws VirtualCollectionRegistryException;

    void updateIdentifier(String pid, URI target) throws VirtualCollectionRegistryException;

    void deleteIdentifier(String pid) throws VirtualCollectionRegistryException;

    boolean ownsIdentifier(String pid);

    boolean isPrimaryProvider();
    void setPrimaryProvider(boolean primary);
}
