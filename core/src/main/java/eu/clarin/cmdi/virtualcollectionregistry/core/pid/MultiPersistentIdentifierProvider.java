package eu.clarin.cmdi.virtualcollectionregistry.core.pid;


import eu.clarin.cmdi.virtualcollectionregistry.core.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

@Service
@Profile("vcr.pid.multi")
public class MultiPersistentIdentifierProvider implements PersistentIdentifierProvider, Serializable {

    /* Providers that will mint identifiers for this collection */
    private final List<PersistentIdentifierProvider> providers;

    @Autowired
    public MultiPersistentIdentifierProvider(List<PersistentIdentifierProvider> providers) {
        this.providers = providers;

        //Check for primary provider
        int primaryProviderCount = 0;
        for(PersistentIdentifierProvider provider : providers) {
            if(provider.isPrimaryProvider()) {
                primaryProviderCount++;
            }
        }
        if(primaryProviderCount != 1) {
            throw new IllegalStateException("Providers list must contain one primary provider, found "+primaryProviderCount);
        }
    }

    @Override
    public String getId() {
        return "multi";
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException {
        return createIdentifier(vc, "", permaLinkService);
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc, String suffix, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException {
        PersistentIdentifier primaryId = null;
        for(PersistentIdentifierProvider provider : providers) {
            PersistentIdentifier id = provider.createIdentifier(vc, permaLinkService);
            if(provider.isPrimaryProvider()) {
                primaryId = id;
            }
        }
        return primaryId;
    }

    @Override
    public void updateIdentifier(PersistentIdentifier pid, URI target) throws VirtualCollectionRegistryException {
        for(PersistentIdentifierProvider otherProvider : providers) {
            if(otherProvider.ownsIdentifier(pid.getActionableURI())) {
                otherProvider.updateIdentifier(pid, target);
            }
        }
    }

    @Override
    public void deleteIdentifier(String pid) throws VirtualCollectionRegistryException {
        for(PersistentIdentifierProvider otherProvider : providers) {
            if(otherProvider.ownsIdentifier(pid)) {
                otherProvider.deleteIdentifier(pid);
            }
        }
    }

    @Override
    public boolean ownsIdentifier(String pid) {
        for(PersistentIdentifierProvider otherProvider : providers) {
            if(otherProvider.ownsIdentifier(pid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPrimaryProvider() {
        return false;
    }

    @Override
    public void setPrimaryProvider(boolean primary) { }

    @Override
    public String getInfix() {
        return null;
    }

    @Override
    public PublicConfiguration getPublicConfiguration() {
        return null;
    }
}
