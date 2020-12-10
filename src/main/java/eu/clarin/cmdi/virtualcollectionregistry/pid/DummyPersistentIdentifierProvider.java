package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("vcr.pid.dummy")
public class DummyPersistentIdentifierProvider implements
        PersistentIdentifierProvider {

    private final String id = "DUMMY";
    private boolean primary = false;

    public DummyPersistentIdentifierProvider() {//throws VirtualCollectionRegistryException {
        super();
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc)
            throws VirtualCollectionRegistryException {
        try {
            Thread.sleep(10000);
        } catch(InterruptedException ex) {}
        return new PersistentIdentifier(vc, PersistentIdentifier.Type.DUMMY, primary,
                "dummy-" + Long.toString(vc.getId()));
    }

    @Override
    public void updateIdentifier(String pid, URI target)
            throws VirtualCollectionRegistryException {
    }

    @Override
    public void deleteIdentifier(String pid)
            throws VirtualCollectionRegistryException {
    }

    @Override
    public boolean ownsIdentifier(String pid) {
        return pid.toLowerCase().startsWith("dummy");
    }

    @Override
    public boolean isPrimaryProvider() {
        return primary;
    }

    @Override
    public void setPrimaryProvider(boolean primary) {
        this.primary = primary;
    }
} // class DummyPersistentIdentifierProvider
