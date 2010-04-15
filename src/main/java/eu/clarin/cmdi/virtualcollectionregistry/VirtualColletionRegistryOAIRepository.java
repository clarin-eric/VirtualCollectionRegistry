package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository;

class VirtualColletionRegistryOAIRepository implements OAIRepository {
	private static final List<String> adminEmailAddresses =
		Arrays.asList("vcr-admin@clarin.eu");
	private static final List<MetadataFormat> supportedFormats = Arrays.asList(
			new MetadataFormat("oai_dc",
							   "http://www.openarchives.org/OAI/2.0/oai_dc/",
							   "http://www.openarchives.org/OAI/2.0/oai_dc.xsd"),
			new MetadataFormat("cmdi",
							   "urn:x-clarin:cmdi-namespace",
							   "http://www.clarin.eu/path/to/schema.xsd")
	);
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
	public String getDescription() {
		return "The virtual collection registry is a component of the " +
			   "CLARIN metadata initiative.";
	}

	@Override
	public Date getEarliestTimestamp() {
		return new Date();
	}
	
	@Override
	public DeletedNotion getDeletedNotion() {
		return DeletedNotion.NO;
	}
	
	@Override
	public Granularity getGranularity() {
		return Granularity.SECONDS;
	}

	@Override
	public List<String> getAdminAddreses() {
		return adminEmailAddresses;
	}

	@Override
	public List<MetadataFormat> getSupportedMetadataFormats() {
		return supportedFormats;
	}

	@Override
	public String getSampleRecordId() {
		return "23";
	}

	@Override
	public List<Object> getSetDescs() {
		return null;
	}

	@Override
	public boolean isValidInternalId(String id) {
		try {
			long i = Long.parseLong(id);
			return (i >= 0);
		} catch (NumberFormatException e) {
			/* IGNORE */
		}
		return false;
	}

} // VirtualColletionRegistryOAIRepository
