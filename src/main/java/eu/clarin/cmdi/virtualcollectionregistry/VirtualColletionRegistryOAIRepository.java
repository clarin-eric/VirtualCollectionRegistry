package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository;

class VirtualColletionRegistryOAIRepository implements OAIRepository {
	private class RecordImpl implements Record {
		private final VirtualCollection vc;

		public RecordImpl(VirtualCollection vc) {
			this.vc = vc;
		}
		
		@Override
		public String getLocalId() {
			return Long.toString(vc.getId());
		}

		@Override
		public Date getDatestamp() {
			return vc.getModifiedDate();
		}


		@Override
		public List<Object> getSetSpec() {
			return null;
		}

		@Override
		public boolean isDeleted() {
			// FIXME: check vc state
			return false;
		}

		@Override
		public List<MetadataFormat> getSupportedMetadataFormats() {
			return supportedFormats;
		}
	} // inner class RecordImpl
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
	private final VirtualCollectionRegistry registry;
	
	VirtualColletionRegistryOAIRepository(VirtualCollectionRegistry registry) {
		this.registry = registry;
	}

	@Override
	public String getId() {
		return "virtualcollectionregistry.cmdi.clarin.eu";
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
	public String getSampleRecordLocalId() {
		return "23";
	}

	@Override
	public List<Object> getSetDescs() {
		return null;
	}

	@Override
	public Object parseLocalId(String unparsedLocalId) {
		try {
			long id = Long.parseLong(unparsedLocalId);
			if (id > 0) {
				return new Long(id);
			}
		} catch (NumberFormatException e) {
			/* FALL-THROUGH */
		}
		return null;
	}

	@Override
	public Record getRecord(Object localId) throws OAIException {
		try {
			long id = (Long) localId;
			VirtualCollection vc = registry.retrieveVirtualCollection(id);
			return new RecordImpl(vc);
		} catch (VirtualCollectionNotFoundException e) {
			return null;
		} catch (VirtualCollectionRegistryException e) {
			throw new OAIException("error", e);
		}
	}

} // VirtualColletionRegistryOAIRepository
