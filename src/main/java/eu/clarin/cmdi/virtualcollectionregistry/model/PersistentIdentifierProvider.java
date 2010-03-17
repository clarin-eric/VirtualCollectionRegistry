package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.Map;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;

public abstract class PersistentIdentifierProvider {
	public static final String BASE_URI = "pid_provider.base_uri";

	protected PersistentIdentifierProvider(Map<String, String> config)
			throws VirtualCollectionRegistryException {
		super();
	}

	public abstract PersistentIdentifier
		createPersistentIdentifier(VirtualCollection vc)
		throws VirtualCollectionRegistryException;

} // interface PersistentIdentifierProvider
