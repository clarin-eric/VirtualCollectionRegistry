package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * An abstract class for implementing Dublin Core converters. This class exists
 * as convenience for creating Dublin Core converter objects.
 *
 * @see DublinCoreConverter
 */
public abstract class DublinCoreAdapter implements DublinCoreConverter {

    @Override
    public boolean canProcessResource(Class<?> clazz) {
        return false;
    }

    @Override
    public List<String> getTitles(Object resource) {
        String value = getTitle(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getTitle(Object resource) {
        return null;
    }
    
    @Override
    public List<String> getCreators(Object resource) {
        String value = getCreator(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getCreator(Object resource) {
        return null;
    }

    @Override
    public List<String> getSubjects(Object resource) {
        String value = getSubject(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getSubject(Object resource) {
        return null;
    }

    @Override
    public List<String> getDescriptions(Object resource) {
        String value = getDescription(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getDescription(Object resource) {
        return null;
    }

    @Override
    public List<String> getPublishers(Object resource) {
        String value = getPublisher(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getPublisher(Object resource) {
        return null;
    }

    @Override
    public List<String> getContributors(Object resource) {
        String value = getContributor(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getContributor(Object resource) {
        return null;
    }

    @Override
    public List<Date> getDates(Object resource) {
        Date value = getDate(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public Date getDate(Object resource) {
        return null;
    }

    @Override
    public List<String> getTypes(Object resource) {
        String value = getType(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getType(Object resource) {
        return null;
    }

    @Override
    public List<String> getFormats(Object resource) {
        String value = getFormat(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getFormat(Object resource) {
        return null;
    }

    @Override
    public List<String> getIdentifiers(Object resource) {
        String value = getIdentifier(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getIdentifier(Object resource) {
        return null;
    }

    @Override
    public List<String> getSources(Object resource) {
        String value = getSource(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getSource(Object resource) {
        return null;
    }

    @Override
    public List<String> getLanguages(Object resource) {
        String value = getLanguage(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getLanguage(Object resource) {
        return null;
    }

    @Override
    public List<String> getRelations(Object resource) {
        String value = getRelation(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getRelation(Object resource) {
        return null;
    }

    @Override
    public List<String> getCoverages(Object resource) {
        String value = getConverage(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getConverage(Object resource) {
        return null;
    }

    @Override
    public List<String> getRights(Object resource) {
        String value = getRight(resource);
        return (value != null) ? Arrays.asList(value) : null;
    }

    public String getRight(Object resource) {
        return null;
    }

} // class DubinCoreConverterAdapter
