package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.model.PersistentIdentifierProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("vcr.pid.dummy")
public class DummyPersistentIdentifierProvider implements
        PersistentIdentifierProvider {

    public DummyPersistentIdentifierProvider() throws VirtualCollectionRegistryException {
        super();
    }

    public PersistentIdentifier createIdentifier(VirtualCollection vc)
            throws VirtualCollectionRegistryException {
        return new PersistentIdentifier(vc, PersistentIdentifier.Type.DUMMY,
                "dummy-" + Long.toString(vc.getId()));
    }

    public void updateIdentifier(String pid, URI target)
            throws VirtualCollectionRegistryException {
    }

    public void deleteIdentifier(String pid)
            throws VirtualCollectionRegistryException {
    }

} // class DummyPersistentIdentifierProvider
