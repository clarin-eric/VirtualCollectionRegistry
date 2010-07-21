package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Date;
import java.util.List;

/**
 * An item in the repository. This adapter interface is used by the OAI provider
 * to get information about an item from the repository.
 */
public interface Record {

    /**
     * The local Id of the record. The returned object must be compatible with
     * repository's implementation of <code>parseLocalId</code> and
     * <code>unparseLocalId</code>.
     * 
     * @returns the local Id object of the record
     * @see Repository#parseLocalId(String)
     * @see Repository#unparseLocalId(Object)
     */
    public Object getLocalId();

    /**
     * Get the last changed timestamp of the item.
     * 
     * @return the last changed timestamp of the item
     */
    public Date getDatestamp();

    /**
     * The deletion state of the item.
     * 
     * @return <code>true</code> of the item is deleted, <code>false</code>
     *         otherwise
     * @see Repository#getGranularity()
     */
    public boolean isDeleted();

    /**
     * The set to which this record belongs to.
     * 
     * @return the list of sets to which the records belongs to or
     *         <code>null</code> if it does not belong to any sets or the
     *         repository does not support set
     */
    public List<String> getSetSpecs();

    /**
     * Get the actual item as the native object. This object is passed to the
     * <code>writeObject</code> method of <code>MetadataFormat</code>.
     * 
     * @return the object as a native object or <code>null</code> if the record
     *         only contains header information
     * @see MetadataFormat#writeObject(eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream,
     *      Object)
     */
    public Object getItem();

    /**
     * Get the class of the native object. The class is needed to determine if a
     * MetadataFormat can write the native object
     * 
     * @return the class of the native object
     * @see MetadataFormat#canWriteClass(Class)
     */
    public Class<?> getItemClass();

} // interface Record
