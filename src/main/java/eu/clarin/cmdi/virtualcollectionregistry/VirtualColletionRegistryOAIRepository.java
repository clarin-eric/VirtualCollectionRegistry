package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream.NamespaceDecl;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.MetadataConstants;

class VirtualColletionRegistryOAIRepository implements OAIRepository {
	private class RecordImpl implements Record {
		private final VirtualCollection vc;

		public RecordImpl(VirtualCollection vc) {
			this.vc = vc;
		}
		
		@Override
		public Object getLocalId() {
			return vc.getId();
		}

		@Override
		public Date getDatestamp() {
			return vc.getModifiedDate();
		}


		@Override
		public List<String> getSetSpec() {
			return Collections.emptyList();
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
		
		@Override
		public Object getItem() {
			return vc;
		}
	} // inner class RecordImpl

	private static class DCMetadataFormat implements MetadataFormat {
		private final static List<NamespaceDecl> dc = Arrays.asList(
				new NamespaceDecl(MetadataConstants.NS_OAI_DC, "oai_dc",
				                  MetadataConstants.NS_OAI_DC_SCHEMA_LOCATION),
				new NamespaceDecl(MetadataConstants.NS_DC, "dc"));

		@Override
		public String getPrefix() {
			return "oai_dc";
		}

		@Override
		public String getNamespaceURI() {
			return "http://www.openarchives.org/OAI/2.0/oai_dc/";
		}

		@Override
		public String getSchemaLocation() {
			return "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
		}

		@Override
		public void writeObject(OAIOutputStream stream, Object item)
				throws OAIException {
			VirtualCollection vc = (VirtualCollection) item;
			stream.writeStartElement(MetadataConstants.NS_OAI_DC, "dc", dc);
			stream.writeStartElement(MetadataConstants.NS_DC, "title");
			stream.writeCharacters(vc.getName());
			stream.writeEndElement(); // dc:title element

			stream.writeStartElement(MetadataConstants.NS_DC, "identifier");
			stream.writeCharacters(vc.getPersistentIdentifier().createURI());
			stream.writeEndElement(); // dc:identifier

			stream.writeStartElement(MetadataConstants.NS_DC, "date");
			// XXX: be sure to use correct date format
			stream.writeDate(vc.getCreationDate());
			stream.writeEndElement(); // dc:date

			if (vc.getCreator() != null) {
				stream.writeStartElement(MetadataConstants.NS_DC, "creator");
				stream.writeCharacters(vc.getCreator().getName());
				stream.writeEndElement(); // dc:creator element
			}

			if (vc.getDescription() != null) {
				stream.writeStartElement(MetadataConstants.NS_DC, "description");
				stream.writeCharacters(vc.getDescription());
				stream.writeEndElement(); // dc:description element
			}
			stream.writeEndElement(); // oai_dc:dc element
		}
	} // class OAIMetadataFormat
	
	private static class CMDIMetadataFormat implements MetadataFormat {
		@Override
		public String getPrefix() {
			return "cmdi";
		}

		@Override
		public String getNamespaceURI() {
			return "urn:x-clarin:cmdi-namespace";
		}

		@Override
		public String getSchemaLocation() {
			return "http://www.clarin.eu/path/to/schema.xsd";
		}

		@Override
		public void writeObject(OAIOutputStream stream, Object item)
				throws OAIException {
		}
	} // class CMDIMetadataFormat
	
	private static final List<String> adminEmailAddresses =
		Arrays.asList("vcr-admin@clarin.eu");
	private static final List<MetadataFormat> supportedFormats =
		Arrays.asList(new DCMetadataFormat(), new CMDIMetadataFormat());
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
	public Object getSampleRecordLocalId() {
		return new Long(23);
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
	public String unparseLocalId(Object localId) {
		return ((Long) localId).toString();
	}

	@Override
	public Record getRecord(Object localId) throws OAIException {
		try {
			long id = (Long) localId;
			VirtualCollection vc = registry.retrieveVirtualCollection(id);
			// FIXME: build record factory
			return new RecordImpl(vc);
		} catch (VirtualCollectionNotFoundException e) {
			return null;
		} catch (VirtualCollectionRegistryException e) {
			throw new OAIException("error", e);
		}
	}

	@Override
	public RecordList getRecords(Date from, Date until, String set, int offset)
			throws OAIException {
		try {
			VirtualCollectionList results =
				registry.getVirtualCollections(null, offset, 2);
			List<VirtualCollection> vcs = results.getItems();
			if (!vcs.isEmpty()) {
				List<Record> records = new ArrayList<Record>(vcs.size());
				for (VirtualCollection vc : vcs) {
					// FIXME: build record factory
					records.add(new RecordImpl(vc));
				}
				int nextOffset =
					results.getOffset() + results.getItems().size();
				if (nextOffset >= results.getTotalCount()) {
					nextOffset = -1;
				}
				return new RecordList(records, nextOffset,
									  results.getTotalCount());
			}
			return null;
		} catch (VirtualCollectionRegistryException e) {
			throw new OAIException("error", e); 
		}
	}

} // VirtualColletionRegistryOAIRepository
