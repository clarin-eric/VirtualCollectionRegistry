package eu.clarin.cmdi.virtualcollectionregistry.oai.repository;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;

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
	 * Check, if objects of a given class can be written by this metadata
	 * format.
	 * 
	 * @param clazz the class of the object
	 * @return <code>true</code> if objects can be disseminated,
     *         <code>false</code> otherwise
	 */
	public boolean canWriteClass(Class<?> clazz);

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
}