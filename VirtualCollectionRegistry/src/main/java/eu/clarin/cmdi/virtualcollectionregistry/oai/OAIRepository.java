package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Date;
import java.util.List;

public interface OAIRepository {
	public enum DeletedNotion {
		NO,
		PERSISTENT,
		TRANSIENT;
	} // enum DeletedNotion
	
	public enum Granularity {
		DAYS,
		SECONDS;
	} // enum Granularity

	public interface MetadataFormat {

		public String getPrefix();

		public String getNamespaceURI();
		
		public String getSchemaLocation();

		public void writeObject(OAIOutputStream stream, Object item)
			throws OAIException;
	} // interface MetadataFormat

	public interface Record {

		public abstract Object getLocalId();

		public abstract Date getDatestamp();

		public abstract List<String> getSetSpec();

		public abstract boolean isDeleted();

		public abstract List<MetadataFormat> getSupportedMetadataFormats();

		public abstract Object getItem();

	} // interface Record

	public final class RecordList {
		private final List<?> items;
		private final int nextOffset;
		private final int totalCount;

		public RecordList(List<?> records, int nextOffset, int totalCount) {
			if (records == null) {
				throw new NullPointerException("records == null");
			}
			this.items = records;
			this.nextOffset = nextOffset;
			this.totalCount = totalCount;
		}

		public List<?> getItems() {
			return items;
		}

		public boolean hasMore() {
			return nextOffset > 0;
		}

		public int getNextOffset() {
			return nextOffset;
		}

		public int getTotalCount() {
			return totalCount > -1 ? totalCount : -1;
		}
	} // class RecordList

	public String getId();

	public String getName();

	public String getDescription();

	public List<String> getAdminAddreses();
	
	public Date getEarliestTimestamp();
	
	public DeletedNotion getDeletedNotion();
	
	public Granularity getGranularity();

	public List<MetadataFormat> getSupportedMetadataFormats();

	public Object getSampleRecordLocalId();

	// FIXME: define class for describing sets
	public List<Object> getSetDescs();

	public Object parseLocalId(String unparsedLocalId);

	public String unparseLocalId(Object localId);

	public Record createRecord(Object item, boolean headerOnly)
		throws OAIException;

	public Object getRecord(Object localId) throws OAIException;

	/*
	 * fetch records matching the criteria. used for ListRecords and
	 * ListIdentifiers verb.
	 * 
	 * @param prefix requested metadata format (e.g "oai_dc")
	 * @param from   optional lower date, may be <code>null</code>
	 * @param until  optional upper date, may be <code>null</code>
	 * @param set    optional set specification, may be <code>null</code>
	 * @param offset start position if resumption request, is <code>-1<code>
	 *               for first request
	 * @returns list of matched records
	 */
	// XXX: identifiers only flag or different method?
	public RecordList getRecords(String prefix, Date from, Date until,
			String set, int offset, boolean headerOnly) throws OAIException;

} // interface OAIRepository
