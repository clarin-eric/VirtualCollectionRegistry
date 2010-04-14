package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.Arrays;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository;

class VirtualColletionRegistryOAIRepository implements OAIRepository {
	private static final List<String> adminEmailAddresses =
		Arrays.asList("vcr-admin@clarin.eu");
	private static final List<String> supportedPrefixes =
		Arrays.asList("oai_dc", "cmdi");
	@SuppressWarnings("unused")
	private final VirtualCollectionRegistry registry;
	
	VirtualColletionRegistryOAIRepository(VirtualCollectionRegistry registry) {
		this.registry = registry;
	}

	@Override
	public String getId() {
		return "eu.clarin.cmdi.VirtualCollectionRegistry";
	}

	@Override
	public String getName() {
		return "CLARIN Virtual Collection Registry";
	}

	@Override
	public List<String> getAdminAddreses() {
		return adminEmailAddresses;
	}

	@Override
	public List<String> getSupportedMetadataPrefixes() {
		return supportedPrefixes;
	}

	@Override
	public String getDescription() {
		return "The virtual collection registry is a component of the " +
			   "CLARIN metadata initiative.";
	}

	@Override
	public String getSampleRecordId() {
		return "23";
	}

} // VirtualColletionRegistryOAIRepository
