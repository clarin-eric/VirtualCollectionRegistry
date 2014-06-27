package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * @author twagoo
 */
@Service
@Profile("vcr.pid.epic2")
public class EPIC2PersistentIdentifierProvider implements PersistentIdentifierProvider {

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateIdentifier(String pid, URI target) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteIdentifier(String pid) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
