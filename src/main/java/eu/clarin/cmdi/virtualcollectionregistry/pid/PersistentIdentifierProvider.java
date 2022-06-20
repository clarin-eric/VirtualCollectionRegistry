package eu.clarin.cmdi.virtualcollectionregistry.pid;

import java.net.URI;
import java.net.URL;

import eu.clarin.cmdi.virtualcollectionregistry.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

public interface PersistentIdentifierProvider {

    /**
     * Return this providers id
     * @return
     */
    String getId();

    /**
     * Resolve the supplied PID
     * @param pid
     * @return
     */
   // URL resolveIdentifier(PersistentIdentifier pid) throws VirtualCollectionRegistryException;

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
