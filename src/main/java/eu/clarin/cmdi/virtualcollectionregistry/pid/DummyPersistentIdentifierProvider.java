package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import java.io.Serializable;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("vcr.pid.dummy")
public class DummyPersistentIdentifierProvider implements
        PersistentIdentifierProvider, Serializable {

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
    public PersistentIdentifier createIdentifier(VirtualCollection vc, PermaLinkService permaLinkService)
            throws VirtualCollectionRegistryException {
        return createIdentifier(vc, "", permaLinkService);
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc, String suffix, PermaLinkService permaLinkService)
            throws VirtualCollectionRegistryException {
        try {
            Thread.sleep(10000);
        } catch(InterruptedException ex) {}
        return new PersistentIdentifier(vc, PersistentIdentifier.Type.DUMMY, primary,
                "dummy-" + Long.toString(vc.getId())+suffix);
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

    @Override
    public String getInfix() {
        return null;
    }

    @Override
    public PublicConfiguration getPublicConfiguration() {
        return null;
    }
} // class DummyPersistentIdentifierProvider
