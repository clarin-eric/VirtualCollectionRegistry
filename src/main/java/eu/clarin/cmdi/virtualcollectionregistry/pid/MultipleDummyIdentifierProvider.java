package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@Profile("vcr.pid.multi")
public class MultipleIdentifierProvider implements PersistentIdentifierProvider {

    @Autowired
    @Qualifier("EPICPersistentIdentifierProvider")
    private PersistentIdentifierProvider primary_provider;

    @Autowired
    @Qualifier("DoiPersistentIdentifierProvider")
    private PersistentIdentifierProvider citable_provider;

    @Override
    public String getId() {
        return "multi";
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc) throws VirtualCollectionRegistryException {
        PersistentIdentifier id = primary_provider.createIdentifier(vc);
        citable_provider.createIdentifier(vc);
        return id;
    }

    @Override
    public void updateIdentifier(String pid, URI target) throws VirtualCollectionRegistryException {
        if(primary_provider.ownsIdentifier(pid)) {
            primary_provider.updateIdentifier(pid, target);
        } else if(citable_provider.ownsIdentifier(pid)) {
            citable_provider.updateIdentifier(pid, target);
        }
    }

    @Override
    public void deleteIdentifier(String pid) throws VirtualCollectionRegistryException {
        if(primary_provider.ownsIdentifier(pid)) {
            primary_provider.deleteIdentifier(pid);
        } else if(citable_provider.ownsIdentifier(pid)) {
            citable_provider.deleteIdentifier(pid);
        }
    }

    @Override
    public boolean ownsIdentifier(String pid) {
        return primary_provider.ownsIdentifier(pid) || citable_provider.ownsIdentifier(pid);
    }
}
