package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.Map;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;

public class InternalPersistentIdentifierProvider extends
		PersistentIdentifierProvider {

	public InternalPersistentIdentifierProvider(Map<String, String> config)
			throws VirtualCollectionRegistryException {
		super(config);
		String prefix = config.get(BASE_URI);
		if (prefix == null) {
			throw new VirtualCollectionRegistryException("configuration " +
					      "parameter \"" + BASE_URI + "\" is not set");
		}
		try {
			InternalPersistentIdentifier.initBaseURI(prefix);
		} catch (Exception e) {
			throw new VirtualCollectionRegistryException("configuration " +
					      "paremeter \"" + BASE_URI + "\" is invalid", e);
		}
	}

	public PersistentIdentifier createPersistentIdentifier(VirtualCollection vc)
			throws VirtualCollectionRegistryException {
		return new InternalPersistentIdentifier(vc);
	}

} // class InternalPersistentIdentifierProvider
