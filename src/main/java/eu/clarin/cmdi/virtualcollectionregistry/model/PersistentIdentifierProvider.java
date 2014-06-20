package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.net.URI;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;

public abstract interface PersistentIdentifierProvider {
    public static final String BASE_URI       = "pid_provider.base_uri";
    public static final String PROVIDER_CLASS = "pid_provider.class";

    PersistentIdentifier createIdentifier(VirtualCollection vc)
            throws VirtualCollectionRegistryException;

    void updateIdentifier(String pid, URI target)
            throws VirtualCollectionRegistryException;

    void deleteIdentifier(String pid)
            throws VirtualCollectionRegistryException;

} 
