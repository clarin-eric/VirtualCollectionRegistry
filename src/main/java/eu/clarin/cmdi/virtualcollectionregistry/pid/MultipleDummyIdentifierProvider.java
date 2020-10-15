package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@Profile("vcr.pid.multi.dummy")
public class MultipleDummyIdentifierProvider implements PersistentIdentifierProvider {

    @Autowired
    @Qualifier("DummyPersistentIdentifierProvider")
    private PersistentIdentifierProvider primaryProvider;

    @Autowired
    @Qualifier("DummyPersistentIdentifierProvider")
    private List<PersistentIdentifierProvider> otherProviders;

    @Override
    public String getId() {
        return "dummy.multi";
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc) throws VirtualCollectionRegistryException {
        PersistentIdentifier id = primaryProvider.createIdentifier(vc);
        for(PersistentIdentifierProvider provider : otherProviders) {
            provider.createIdentifier(vc);
        }
        return id;
    }

    @Override
    public void updateIdentifier(String pid, URI target) throws VirtualCollectionRegistryException {
        /*
        if(primary_provider.ownsIdentifier(pid)) {
            primary_provider.updateIdentifier(pid, target);
        } else if(citable_provider.ownsIdentifier(pid)) {
            citable_provider.updateIdentifier(pid, target);
        }
         */
    }

    @Override
    public void deleteIdentifier(String pid) throws VirtualCollectionRegistryException {
        /*
        if(primary_provider.ownsIdentifier(pid)) {
            primary_provider.deleteIdentifier(pid);
        } else if(citable_provider.ownsIdentifier(pid)) {
            citable_provider.deleteIdentifier(pid);
        }
         */
    }

    @Override
    public boolean ownsIdentifier(String pid) {
        if(primaryProvider.ownsIdentifier(pid)) {
            return true;
        }

        for(PersistentIdentifierProvider provider : otherProviders) {
            if(provider.ownsIdentifier(pid)) {
                return true;
            }
        }

        return false;
    }
}
