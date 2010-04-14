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
	} // class MetadataFormat

	public String getId();
	
	public String getName();
	
	public List<String> getAdminAddreses();
	
	public Date getEarliestTimestamp();
	
	public DeletedNotion getDeletedNotion();
	
	public Granularity getGranularity();

	public List<MetadataFormat> getSupportedMetadataFormats();

	public String getDescription();

	public String getSampleRecordId();

} // interface OAIRepository
