package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Date;
import java.util.List;

public interface OAIRepository {
	/** flag for gzip compression method */
	public static final int COMPRESSION_METHOD_GZIP    = 0x01;
	/** flag for deflate compression method */
	public static final int COMPRESSION_METHOD_DEFLATE = 0x02;

	/**
	 * The manner in which the repository supports the notion of
	 * deleted records.
	 */
	public enum DeletedNotion {
		/** Repository has no information about deleted records */
		NO,
		/** Repository keeps information of deleted records wit no
            time limit */
		PERSISTENT,
		/** Repository does not guarantee that a list of deletions
            of maintained persistently or consistently */
		TRANSIENT;
	} // enum DeletedNotion
	

	/**
	 * The timestamp granularity the repository supports.
	 */
	public enum Granularity {
		/** The repository supports days granularity timestamps */
		DAYS,
		/** The repository supports seconds granularity timestamps */
		SECONDS;
	} // enum Granularity


	/**
	 * Interface for metadata format implementations. The OAI provider
	 * uses this interface to talk to the metadata format converter.
	 */
	public interface MetadataFormat {

		/**
		 * Get the prefix for this metadata format. Used by the
		 * <em>ListMetadataFormats</em>, <em>GetRecord</em> and
		 * <em>ListRecords</em> verbs.
		 * 
		 * @return the prefix for metadata format 
		 */
		public String getPrefix();

		/**
		 * Get the namespace URI for this metadata format. Used by the
		 * <em>ListMetadataFormats</em> <em>GetRecord</em> and
		 * <em>ListRecords</em> verbs. Even though the return type of this
		 * method is String, it should return a well-formed URI.
		 * 
		 * @return the namespace URI for this metadata format
		 * @see java.net.URI
		 */
		public String getNamespaceURI();
		
		/**
		 * Get the schema location URI for this metadata format. Used by the
		 * <em>ListMetadataFormats</em> <em>GetRecord</em> and
		 * <em>ListRecords</em> verbs. Even though the return type of this
		 * method is String, it should return a well-formed URI.
		 * 
		 * @return the schema location URI for this metadata format
		 * @see java.net.URI
		 */
		public String getSchemaLocation();

		/**
		 * Transform an object into the metadata format and write it
		 * to the output stream. Used by the <em>GetRecord</em> and
		 * <em>ListRecords</em> verbs.
		 * 
		 * @param stream  the output stream
		 * @param item    the object, which is to be written
		 * @throws OAIException if an error occurs
		 */
		public void writeObject(OAIOutputStream stream, Object item)
			throws OAIException;
	} // interface MetadataFormat

	public final class SetSpecDesc {
	    private final String id;
	    private final String name;
	    private final String description;

	    public SetSpecDesc(String id, String name, String description) {
	        this.id          = id;
	        this.name        = name;
	        this.description = description;
	    }
	    
	    public SetSpecDesc(String id, String name) {
	        this(id, name, null);
	    }

	    public String getId() {
	        return id;
	    }
	    
	    public String getName() {
	        return name;
	    }

	    public String getDescription() {
	        return description;
	    }
	} // class SetSpecDesc
	
	/**
	 * An item in the repository. This interface is used by the OAI provider
	 * to get information about an item from the repository.
	 */
	public interface Record {

		/**
		 * The local Id of the record. The returned object must be compatible
		 * with repository's implementation of <code>parseLocalId</code> and
		 * <code>unparseLocalId</code>.
		 * 
		 *  @returns the local Id object of the record
		 *  @see parseLocalId
		 *  @see unparseLocalId
		 */
		public abstract Object getLocalId();

		/**
		 * Get the last changed timestamp of the item.
		 * 
		 * @return the last changed timestamp of the item
		 */
		public abstract Date getDatestamp();

		/**
		 * Get the sets to which the item belongs to.
		 * 
		 * @return the list of sets to which the item belongs or
		 *		   <code>null</code> if item does not belong to any set.
		 */
		public abstract List<String> getSetSpec();

		/**
		 * The deletion state of the item.
		 * 
		 * @return <code>true</code> of the item is deleted,
		 *         <code>false</code> otherwise
		 * @see getGranularity
		 */
		public abstract boolean isDeleted();

		/**
		 * A list of metadata formats in which the item can be represented. 
		 *
		 * @return a list of metadata formats
		 * @see MetadataFormat 
		 */
		public abstract List<MetadataFormat> getSupportedMetadataFormats();

		/**
		 * The actual item as the native object. This object is passed to
		 * the <code>writeObject</code> method of <code>MetadataFormat</code>.
		 * 
		 * @return the object as a native object
		 * @see MetadataFormat
		 */
		public abstract Object getItem();

	} // interface Record

	
	/**
	 * A list of items from the repository as the result of a query. Used
	 * by the <em>ListRecords</em> and <em>ListIdentifiers</em> verb. This
	 * can also be a partial list if the repository wants do flow-control.
	 */
	public final class RecordList {
		private final List<?> items;
		private final int nextOffset;
		private final int totalCount;

		/**
		 * Constructor.
		 * 
		 * @param records     the list objects as native objects from the
		 *                    repository
		 * @param nextOffset  the next valid offset or <code>-1</code> if
		 *                    there are no more results
		 * @param totalCount  the total count of records
		 */
		public RecordList(List<?> records, int nextOffset, int totalCount) {
			if (records == null) {
				throw new NullPointerException("records == null");
			}
			this.items = records;
			this.nextOffset = nextOffset;
			this.totalCount = totalCount;
		}

		/**
		 * Get the list of items
		 * 
		 * @return the list of items
		 */
		public List<?> getItems() {
			return items;
		}

		/**
		 * Check of the list contains more results. Used for flow-control
		 * 
		 * @return <code>true</code> if the list contains more results,
		 *         <code>false</code> otherwise
		 */
		public boolean hasMore() {
			return nextOffset > 0;
		}

		/**
		 * Get the next offset in the result list. Used for flow-control.
         *
		 * @return the next offset
		 */
		public int getNextOffset() {
			return nextOffset;
		}

		/**
		 * Get the total number of items. Used for flow-control.
         *
		 * @return the total number of items
		 */
		public int getTotalCount() {
			return totalCount > -1 ? totalCount : -1;
		}
	} // class RecordList

	
	/**
	 * Get the id of the repository. Used for parsing and creating of
	 * OAI identifiers. 
	 * 
	 * @return id of the repository 
	 */
	public String getId();

	/**
	 * Get the name of the repository. Used by the Identify</em> verb.
	 * 
	 * @return the name of the repository
	 */
	public String getName();

	/**
	 * Get a human readable description for the repository. Used by the
     * <em>Identify</em> verb to add Dublin Core metadata to the response.
     * 
	 * @return the description of the repository
	 */
	public String getDescription();

	/**
	 * Get the e-mail address of an administrator of the repository. The result
	 * must contain at least one address. Used for <em>Identify</em> verb.
	 * 
	 * @return the list of e-mail addresses
	 */
	public List<String> getAdminAddreses();
	
	/**
	 * Get the guaranteed lower limit of all datestamps recording changes,
     * modifications, or deletions in the repository. Used for
	 * <em>Identify</em> verb.
     * 
	 * @return the earliest timestamp
	 */
	public Date getEarliestTimestamp();
	
	/**
	 * Get the manner in which the repository supports the notion of
	 * deleted records. Used for <em>Identify</em> verb.
	 * 
	 * @return the notion of deleted records
	 * @see DeletedNotion
	 */
	public DeletedNotion getDeletedNotion();
	
	/**
	 * Get the granularity of the repository. The provider automatically
     * adjusts timestamps as necessary. Used for <em>Identify</em> verb.
	 *  
	 * @return the granularity of the repository.
	 * @see Granularity
	 */
	public Granularity getGranularity();

	/**
	 * Get the compression encoding methods this repository supports.
	 * 
	 * @return the compression encodings methods flag set.
	 * @see #COMPRESSION_METHOD_GZIP
	 * @see #COMPRESSION_METHOD_DEFLATE
	 */
	public int getSupportedCompressionMethods();
	
	/**
	 * Get the list of metadata formats, which are supported by the repository.
	 * A repository always must support the Dublin Core metadata with the
 	 * prefix <em>oai_dc</em>. Used for <em>ListMetadataList</em> verb.
	 * 
	 * @return the list of supported metadata formats
	 * @see MetadataFormat
	 */
	public List<MetadataFormat> getSupportedMetadataFormats();

	/**
	 * Get a sample local id object. Used for the <em>Identify</verb>. The
	 * actual value does not matter.
	 * 
	 * @return a sample local id object
	 */
	public Object getSampleRecordLocalId();

	public List<SetSpecDesc> getSetDescs();

	/**
	 * Convert the string representation of a local id to a local id object.
	 * A repository if free to use any representation for its local identifiers.
	 * The provider used <code>parseLocalId</code> and
     * <code>unparseLocalId</code> to convert between the string and the object
     * representation of the local id. When talking to the repository, the
     * provider always uses the object representation. 
	 *  
	 * @param unparsedLocalId  local id in string representation
	 * @return                 the local id object or <code>null</code> if the
	 *                         string representation could not be converted
     *                         (e.g. due to an invalid format)
     * @see unparseLocalId
	 */
	public Object parseLocalId(String unparsedLocalId);

	/**
	 * Convert a local id object to a string representation.
	 * 
	 * @param localId  local id object
	 * @return         the string representation of the local id object
	 */
	public String unparseLocalId(Object localId);

	/**
	 * Create an wrapper for a given item with abstracts the actual
	 * implementation of the item from the implementation of the provider.
	 *
	 * @param item              the item for which the wrapper need to
     *                          be created
	 * @param headerOnly        <code>true</code> if the provider only needs
	 *                          enough information form the repository to
     *                          generate item header information
	 * @return                  the record wrapper for the item
	 * @exception OAIException  something went wrong creating the wrapper
	 * @see Record   
	 */
	public Record createRecord(Object item, boolean headerOnly)
		throws OAIException;

	/**
	 * Fetch an item from the repository matching a given id.
	 * Used for <em>GetRecord</em> and <em>ListMetadataFormat<em> verbs.
	 * 
	 * @param localId     the local id object of the item, which is requested
	 *                    by the provider
	 * @param headerOnly  <code>true</code> if the provider only needs
	 *                    enough information form the repository to generate
	 *                    item header information and not the complete item
	 * @returns           the requested item or <code>null</code> if none
     *                    was found
     * @see parseLocalId   
	 */
	public Object getRecord(Object localId) throws OAIException;

	/**
	 * Fetch items from the repository matching the given criteria.
	 * Used for <em>ListRecords</em> and <em>ListIdentifiers</em> verbs.
	 * 
	 * @param prefix      requested metadata format (e.g "oai_dc")
	 * @param from        optional lower date, may be <code>null</code>
	 * @param until       optional upper date, may be <code>null</code>
	 * @param set         optional set specification, may be <code>null</code>
	 * @param offset      start position if resumption request,
                          is <code>-1<code> for the first request
	 * @param headerOnly  <code>true</code> if the provider only needs
	 *                    enough information form the repository to generate
	 *                    item header information and not the complete item
	 * @returns           the list of matched items or <code>null</code> if
	 *                    none matched the criteria
	 * @see RecordList
	 */
	public RecordList getRecords(String prefix, Date from, Date until,
			String set, int offset, boolean headerOnly) throws OAIException;

} // interface OAIRepository
