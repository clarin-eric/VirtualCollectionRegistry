package eu.clarin.cmdi.oai.provider;

import java.util.Date;
import java.util.Set;


/**
 * Interface for the OAI provider for talking to the repository. A repository
 * needs to implements this interface in order to be used with the OAI provider.
 */
public interface Repository {
    /** flag for gzip compression method */
    public static final int COMPRESSION_GZIP = 0x01;
    /** flag for deflate compression method */
    public static final int COMPRESSION_DEFLATE = 0x02;

    /**
     * The manner in which the repository supports the notion of deleted
     * records.
     */
    public enum DeletedNotion {
        /**
         * Repository has no information about deleted records
         */
        NO,
        /**
         * Repository keeps information of deleted records wit no time limit
         */
        PERSISTENT,
        /**
         * Repository does not guarantee that a list of deletions of maintained
         * persistently or consistently
         */
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
     * Get the id of the repository. Used for parsing and creating of OAI
     * identifiers.
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
     * must contain at least one address. Used for <em>Identify</em> verb. This
     * value will be cached by the OAI provider upon initialization.
     * 
     * @return the list of e-mail addresses
     */
    public Set<String> getAdminAddreses();

    /**
     * Get the guaranteed lower limit of all datestamps recording changes,
     * modifications, or deletions in the repository. Used for <em>Identify</em>
     * verb. This value will be cached by the OAI provider upon initialization.
     * 
     * @return the earliest timestamp
     */
    public Date getEarliestTimestamp();

    /**
     * Get the manner in which the repository supports the notion of deleted
     * records. Used for <em>Identify</em> verb.
     * 
     * @return the notion of deleted records
     * @see DeletedNotion
     */
    public DeletedNotion getDeletedNotion();

    /**
     * Get the granularity of the repository. The provider automatically adjusts
     * timestamps as necessary. Used for <em>Identify</em> verb.
     * 
     * @return the granularity of the repository.
     * @see Granularity
     */
    public Granularity getGranularity();

    /**
     * Get the compression encoding methods this repository supports.
     * 
     * @return the compression encodings methods flag set.
     * @see #COMPRESSION_GZIP
     * @see #COMPRESSION_DEFLATE
     */
    public int getCompressionMethods();

    /**
     * Get a sample local id object. Used for the <em>Identify</verb>. The
	 * actual value does not matter.
     * 
     * @return a sample local id object
     */
    public Object getSampleRecordLocalId();

    /**
     * Get a list of converters to for handling the "oai_dc" metadata prefix. 
     * Used for the <em>GetRecord</em> verb. This value will be cached by
     * the OAI provider upon initialization.
     * 
     * @return the list of Dublin Core converters
     * @see DublinCoreConverter
     */
    public Set<DublinCoreConverter> getDublinCoreConverters();

    /**
     * Get the custom metadata formats, which are supported by the
     * repository. This set <b>must not</b> include a metadata format for the
     * <em>oai_dc</em> prefix.
     * Used for the <em>ListMetadataFormats</em> and <em>GetRecord</em> verbs.
     * This value will be cached by the OAI provider upon initialization.
     * 
     * @return the list of supported metadata formats
     * @see MetadataFormat
     * @see #getDublinCoreConverters()
     */
    public Set<MetadataFormat> getCustomMetadataFormats();

    /**
     * Get the sets, which are generally supported by the repository. Used for
     * the <em>ListSets</em> verb. This value will be cached by the OAI provider
     * upon initialization.
     * 
     * @return the list of supported sets
     * @see SetSpecDesc
     */
    public Set<SetSpecDesc> getSetDescs();

    /**
     * Convert the string representation of a local id to a local id object. A
     * repository if free to use any representation for its local identifiers.
     * The provider used <code>parseLocalId</code> and
     * <code>unparseLocalId</code> to convert between the string and the object
     * representation of the local id. When talking to the repository, the
     * provider always uses the object representation.
     * 
     * @param unparsedLocalId
     *            local id in string representation
     * @return the local id object or <code>null</code> if the string
     *         representation could not be converted (e.g. due to an invalid
     *         format)
     * @see unparseLocalId
     */
    public Object parseLocalId(String unparsedLocalId);

    /**
     * Convert a local id object to a string representation.
     * 
     * @param localId
     *            local id object
     * @return the string representation of the local id object
     */
    public String unparseLocalId(Object localId);

    /**
     * Fetch an item from the repository matching a given id. Used for
     * <em>GetRecord</em> and <em>ListMetadataFormat<em> verbs.
     * 
     * @param localId
     *            the local id object of the item, which is requested by the
     *            provider
     * @param headerOnly
     *            <code>true</code> if the provider only needs enough
     *            information from the repository to generate item header
     *            information and not the complete item
     * @returns the requested item or <code>null</code> if none was found
     * @throws OAIException if an error occurred
     * @see parseLocalId
     */
    public Record getRecord(Object localId, boolean headerOnly)
            throws OAIException;

    /**
     * Fetch items from the repository matching the given criteria. Used for
     * <em>ListRecords</em> and <em>ListIdentifiers</em> verbs.
     * 
     * @param prefix
     *            requested metadata format (e.g "oai_dc")
     * @param from
     *            optional lower date, may be <code>null</code>
     * @param until
     *            optional upper date, may be <code>null</code>
     * @param set
     *            optional set specification, may be <code>null</code>
     * @param offset
     *            start position if resumption request, is
     *            <code>-1<code> for the first request
     * @param headerOnly
     *            <code>true</code> if the provider only needs enough
     *            information from the repository to generate item header
     *            information and not the complete item
     * @returns the list of matched items or <code>null</code> if none matched
     *          the criteria
     * @throws OAIException if an error occurred
     * @see RecordList
     */
    public RecordList getRecords(String prefix, Date from, Date until,
            String set, int offset, boolean headerOnly) throws OAIException;

} // interface Repository
