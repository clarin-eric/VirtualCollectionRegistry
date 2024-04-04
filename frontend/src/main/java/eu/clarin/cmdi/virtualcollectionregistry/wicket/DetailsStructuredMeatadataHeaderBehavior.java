package eu.clarin.cmdi.virtualcollectionregistry.wicket;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.wicket.model.JsonLdModel;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class DetailsStructuredMeatadataHeaderBehavior extends JsonLdHeaderBehavior {

    public final static SimpleDateFormat SDF_ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    public static final int DESCRIPTION_MIN_LENGTH = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 5000;

    public DetailsStructuredMeatadataHeaderBehavior(IModel<VirtualCollection> collectionModel) {
        super(new JsonLdModel(new JsonLoadableDetachableModel(collectionModel)));
    }

    @Override
    public boolean isEnabled(Component component) {
        return super.isEnabled(component);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        if (isEnabled(component)) {
            super.renderHead(component, response);
        }
    }

    private static class JsonLoadableDetachableModel extends  LoadableDetachableModel<JsonLdModel.JsonLdObject> {
        private final IModel<VirtualCollection> collectionModel;

        public JsonLoadableDetachableModel(IModel<VirtualCollection> collectionModel) {
            this.collectionModel = collectionModel;
        }

        @Override
        protected JsonLdModel.JsonLdObject load() {
            if (collectionModel.getObject() == null) {
                return null;
            } else {
                return createDataSetForDocument(collectionModel);
            }
        }

        private JsonLdCollection createDataSetForDocument(IModel<VirtualCollection> collectionModel) {
            final JsonLdCollection dataSet = new JsonLdCollection();

            dataSet.setUrl(Application.get().getPermaLinkService().getCollectionUrl(collectionModel.getObject()));
            dataSet.setName(collectionModel.getObject().getName());
            dataSet.setDescription(fixDescriptionLength(collectionModel.getObject().getDescription()));
            final DataCatalog dataCatalog = new DataCatalog(Application.get().getPermaLinkService().getBaseUri());
            dataSet.setIncludedInDataCatalog(dataCatalog);

            for(PersistentIdentifier id : collectionModel.getObject().getIdentifiers()) {
                dataSet.addIdentifier(id.getActionableURI());
            }

            for(Creator c : collectionModel.getObject().getCreators()) {
                dataSet.addCreator(new Person(c.getPerson()));
            }

            for(Resource r : collectionModel.getObject().getResources()) {
                dataSet.addHasPart(new CreativeWork(r.getRef(), r.getLabel(), r.getDescription()));
            }

            dataSet.setDateCreated(formatTimestamp(collectionModel.getObject().getCreationDate()));
            dataSet.setDateModified(formatTimestamp(collectionModel.getObject().getDateModified()));
            dataSet.setDatePublished(formatTimestamp(collectionModel.getObject().getDatePublished()));
            return dataSet;
        }

        private String formatTimestamp(Date d) {
            if(d == null) {
                return null;
            }
            return SDF_ISO_8601.format(d);
        }
        /**
         * Ensures value validity for description; if necessary transforms the
         * string to meet string length requirements
         *
         * @param description
         * @return
         */
        private static String fixDescriptionLength(String description) {
            if (description == null) {
                return null;
            } else {
                if (description.length() < DESCRIPTION_MIN_LENGTH) {
                    // Too short: repeat until long enough
                    final StringBuilder longerDescriptionBuilder = new StringBuilder(description);
                    while (longerDescriptionBuilder.length() < DESCRIPTION_MIN_LENGTH) {
                        longerDescriptionBuilder.append("; ").append(description);
                    }
                    return longerDescriptionBuilder.toString();
                } else if (description.length() > DESCRIPTION_MAX_LENGTH) {
                    // Too long: truncate
                    return description.substring(0, DESCRIPTION_MAX_LENGTH - 1);
                } else {
                    return description;
                }
            }
        }
    }
/*
    //https://schema.org/Dataset
    private static class DataSet extends JsonLdModel.JsonLdObject {
        private String url;
        private String name;
        private String description;
        private Collection<String> identifier;
        private DataCatalog includedInDataCatalog;
        private Collection<Person> creator;
        //https://schema.org/hasPart
        private Collection<CreativeWork> hasPart;
        private String dateCreated;
        private String dateModified;
        private String datePublished;

        public DataSet() {
            super("https://schema.org", "DataSet");
        }
*/
    private static class JsonLdCollection extends JsonLdModel.JsonLdObject {
        private String url;
        private String name;
        private String description;
        private Collection<String> identifier;
        private DataCatalog includedInDataCatalog;
        private Collection<Person> creator;
        //https://schema.org/hasPart
        private Collection<CreativeWork> hasPart;
        private String dateCreated;
        private String dateModified;
        private String datePublished;

        public JsonLdCollection() {
            super("https://schema.org", "Collection");
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Collection<String> getIdentifier() {
            return identifier;
        }

        public void setIdentifier(Collection<String> identifier) {
            this.identifier = identifier;
        }

        public void addIdentifier(String id) {
            if(this.identifier == null) {
                this.identifier = new LinkedList<>();
            }
            this.identifier.add(id);
        }

        public DataCatalog getIncludedInDataCatalog() {
            return includedInDataCatalog;
        }

        public void setIncludedInDataCatalog(DataCatalog includedInDataCatalog) {
            this.includedInDataCatalog = includedInDataCatalog;
        }

        public Collection<Person> getCreator() {
            return creator;
        }

        public void setCreator(Collection<Person> creator) {
            this.creator = creator;
        }

        public void addCreator(Person creator) {
            if(this.creator == null) {
                this.creator = new LinkedList<>();
            }
            this.creator.add(creator);
        }

        public Collection<CreativeWork> getHasPart() {
            return hasPart;
        }

        public void setHasPart(Collection<CreativeWork> hasPart) {
            this.hasPart = hasPart;
        }

        public void addHasPart(CreativeWork work) {
            if(this.hasPart == null) {
                this.hasPart = new LinkedList<>();
            }
            this.hasPart.add(work);
        }

        public String getDateCreated() {
            return dateCreated;
        }

        public String getDatePublished() {
            return datePublished;
        }

        public void setDateCreated(String dateCreated) {
            this.dateCreated = dateCreated;
        }

        public void setDatePublished(String datePublished) {
            this.datePublished = datePublished;
        }

        public String getDateModified() {
            return dateModified;
        }

        public void setDateModified(String dateModified) {
            this.dateModified = dateModified;
        }
    }

    //https://schema.org/DataCatalog
    private static class DataCatalog extends JsonLdModel.JsonLdObject {
        private final String url;

        public DataCatalog(String url) {
            super("DataCatalog");
            this.url = url;
        }

        public String getUrl() { return url; }
    }

    //https://schema.org/Person
    private static class Person extends JsonLdModel.JsonLdObject {
        private final String name;

        public Person(String name) {
            super("Person");
            this.name = name;
        }

        public String getName() { return name; }
    }

    //https://schema.org/CreativeWork
    private static class CreativeWork extends JsonLdModel.JsonLdObject {
        private final String url;
        private final String name;
        private final String description;

        public CreativeWork(String url, String name, String description) {
            super("CreativeWork");
            this.url = url;
            this.name = name;
            this.description = description;
        }

        public String getUrl() { return url; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}
