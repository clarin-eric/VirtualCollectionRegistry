package eu.clarin.cmdi.virtualcollectionregistry;

import java.net.URI;
import java.util.Map;

import eu.clarin.cmdi.virtualcollectionregistry.model.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.model.PersistentIdentifierProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

public class DummyPersistentIdentifierProvider extends PersistentIdentifierProvider {

	public DummyPersistentIdentifierProvider(Map<String, String> config)
			throws VirtualCollectionRegistryException {
		super(config);
	}

	public PersistentIdentifier createIdentifier(VirtualCollection vc)
			throws VirtualCollectionRegistryException {
		return doCreate(vc, PersistentIdentifier.Type.DUMMY, vc.getUUID());
	}

	public void updateIdentifier(String pid, URI target)
			throws VirtualCollectionRegistryException {
	}

	public void deleteIdentifier(String pid)
			throws VirtualCollectionRegistryException {
	}

} // class DummyPersistentIdentifierProvider
