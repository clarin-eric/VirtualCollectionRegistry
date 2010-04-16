package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Date;
import java.util.List;

public interface OAIRepository {
	public static enum DeletedNotion {
		NO,
		PERSISTENT,
		TRANSIENT;
	} // enum DeletedNotion
	
	public static enum Granularity {
		DAYS,
		SECONDS;
	} // enum Granularity

	public static class MetadataFormat {
		private final String prefix;
		private final String namespaceURI;
		private final String schemaLocation;
		
		public MetadataFormat(String prefix, String namespaceURI, String schemaLocation) {
			if (prefix == null) {
				throw new NullPointerException("prefix == null");
			}
			this.prefix = prefix;
			if (namespaceURI == null) {
				throw new NullPointerException("namespaceURI == null");
			}
			this.namespaceURI = namespaceURI;
			if (schemaLocation == null) {
				throw new NullPointerException("schemaLocation == null");
			}
			this.schemaLocation = schemaLocation;
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public String getNamespaceURI() {
			return namespaceURI;
		}
		
		public String getSchemaLocation() {
			return schemaLocation;
		}
	} // inner class MetadataFormat

	public interface Record {

		public String getId();

		public Date getDatestamp();

		// FIXME: define how to represent setSpecs
		public List<Object> getSetSpec();

		public boolean isDeleted();
		
		public List<MetadataFormat> getSupportedMetadataFormats();
	} // interface Record

	public String getId();

	public String getName();

	public String getDescription();

	public List<String> getAdminAddreses();
	
	public Date getEarliestTimestamp();
	
	public DeletedNotion getDeletedNotion();
	
	public Granularity getGranularity();

	public List<MetadataFormat> getSupportedMetadataFormats();

	public String getSampleRecordId();

	// FIXME: define class for describing sets
	public List<Object> getSetDescs();

	public boolean checkLocalId(String id);

	// XXX: OAIRepositoryException?
	public Record getRecord(String id) throws OAIException;
	
} // interface OAIRepository
