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

	public static interface MetadataFormat {

		public String getPrefix();

		public String getNamespaceURI();
		
		public String getSchemaLocation();

		public void writeObject(OAIOutputStream stream, Object item)
			throws OAIException;
	} // interface MetadataFormat

	public interface Record {

		public Object getLocalId();

		public Date getDatestamp();

		public List<String> getSetSpec();

		public boolean isDeleted();

		public List<MetadataFormat> getSupportedMetadataFormats();

		public Object getItem();
	} // interface Record

	public class RecordList {
		private final List<Record> records;
		private final int nextOffset;
		private final int totalCount;

		public RecordList(List<Record> records, int nextOffset, int totalCount) {
			if (records == null) {
				throw new NullPointerException("records == null");
			}
			this.records = records;
			this.nextOffset = nextOffset;
			this.totalCount = totalCount;
		}

		public List<Record> getRecords() {
			return records;
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

	// XXX: OAIRepositoryException?
	public Record getRecord(Object localId) throws OAIException;

	/*
	 * fetch records matching the criteria. used for ListRecords and
	 * ListIdentifiers verb.
	 * 
	 * @param from   optional lower date, may be <code>null</code>
	 * @param until  optional upper date, may be <code>null</code>
	 * @param set    optional set specification, may be <code>null</code>
	 * @param offset start position if resumption request, is <code>-1<code>
	 *               for first request
	 * @returns list of matched records
	 */
	// XXX: identifiers only flag or different method?
	public RecordList getRecords(Date from, Date until, String set, int offset)
		throws OAIException;

} // interface OAIRepository
