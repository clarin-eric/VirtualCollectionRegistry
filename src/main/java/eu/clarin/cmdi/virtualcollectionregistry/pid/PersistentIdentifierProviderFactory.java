package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

@Service
@Profile("vcr.pid.multi")
public class PersistentIdentifierProviderFactory implements PersistentIdentifierProvider {


    /*

    class A {
        Field a;
        Field b;
    }

    class Test1 implements Field {
    }

    class Test2 implements Field {
    }

     */


    /* Provider responsible to mint primary identifiers */
    private final PersistentIdentifierProvider primaryProvider;

    /* Other providers that will mint identifiers for this collection */
    private final List<PersistentIdentifierProvider> otherProviders = new LinkedList<>();

    public PersistentIdentifierProviderFactory() {
        primaryProvider = new EPICPersistentIdentifierProvider();
        otherProviders.add(new DoiPersistentIdentifierProvider());
    }

    @Override
    public String getId() {
        return "multi";
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
