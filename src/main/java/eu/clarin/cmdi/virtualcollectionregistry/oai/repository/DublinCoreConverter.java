package eu.clarin.cmdi.virtualcollectionregistry.oai.repository;

import java.util.Date;
import java.util.List;

/**
 * Interface for converters used to extract Dublin Core metadata from
 * native resource objects. Each converter supports one class of resources
 * and is responsible to extract the nessecary metadata from it. See Dublin
 * Core element specification for the indented semantics of the elements.
 * Implementations need to be thread-safe and without side effects.
 * 
 */
public interface DublinCoreConverter {

    /**
     * Check, if resources of a given class can be processed by this converter.
     * 
     * @param clazz the class of resources which may be processed
     *              using this converter
     * @return <code>true</code> if objects can be handled,
     *         <code>false</code> otherwise
     */
    public boolean canProcessResource(Class<?> clazz);

    /**
     * Extract names of a resource. Values are used for the Dublin Core
     * element <em>dc:title</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getTitles(Object resource);
    
    /**
     * Extract creators of resource. Values are used for the Dublin Core
     * element <em>dc:creator</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getCreators(Object resource);
    
    /**
     * Extract the topics of resource. Values are used for the Dublin Core
     * element <em>dc:subject</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getSubjects(Object resource);

    /**
     * Extract descriptions for a resource. Values are used for the Dublin
     * Core element <em>dc:description</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getDescriptions(Object resource);

    /**
     * Extract the publishers. Values are used for the Dublin Core element
     * <em>dc:publisher</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getPublishers(Object resource);

    /**
     * Extract entities contributing to a resource. Values are used for the
     * Dublin Core element <em>dc:contributor</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getContributors(Object resource);

    /**
     * Extract dates associated to the life-cycle of a resource. Values are
     * used for the Dublin Core element <em>dc:date</em>. The date objects are
     * automatically converted to String representation by the repository. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<Date> getDates(Object resource);

    /**
     * Extract the nature or genre of a resource. Values are used for the
     * Dublin Core element <em>dc:type</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getTypes(Object resource);

    /**
     * Extract file format, physical medium, or dimensions of the resource.
     * Values are used for the Dublin Core element <em>dc:format</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getFormats(Object resource);

    /**
     * Extract identifiers for a resource. Values are used
     * for the Dublin Core element <em>dc:identifier</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getIdentifiers(Object resource);

    /**
     * Extract related resources from which the resource is derived. Values
     * are used for the Dublin Core element <em>dc:source</em>. 
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getSources(Object resource);

    /**
     * Extract the languages of a resource. Values are used for the Dublin Core
     * element <em>dc:languages</em>. Values should be compliant to RFC 4646.
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getLanguages(Object resource);

    /**
     * Extract the related resource. Values are used for the Dublin Core
     * element <em>dc:relation</em>.
     *
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getRelations(Object resource);

    /**
     * Extract spatial or temporal topic of the resource, the spatial
     * applicability of the resource, or the jurisdiction under which the
     * resource is relevant. Values are used for the Dublin Core
     * element <em>dc:coverage</em>.
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getCoverages(Object resource);

    /**
     * Extract information about rights held in and over the resource. Values
     * are used for the Dublin Core element <em>dc:rights</em>.
     * 
     * @param resource a resource object
     * @return a set of values or <code>null</code>, if none are present.  
     */
    public List<String> getRights(Object resource);
    
} // interface DublinCoreConverter
