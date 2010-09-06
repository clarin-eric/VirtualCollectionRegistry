package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "description", "query" },
         namespace = "urn:x-vcr:generatedby")
@XmlSeeAlso(GeneratedBy.Query.class)
public class GeneratedBy {
    @Embeddable
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Query {
        @Column(name = "generatedby_query_profile")
        @XmlAttribute(name = "profile", required = true)
        private String profile;
        @Column(name = "generatedby_query_value")
        @Lob
        @XmlValue
        private String value;

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public String getProfile() {
            return profile;
        }

        public void setQuery(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    } // class Query

    @Column(name = "generatedby_description")
    private String description;
    @Embedded
    private Query query;

    public GeneratedBy() {
        super();
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @XmlElement(name = "Description")
    public String getDescription() {
        return description;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    @XmlElement(name = "Query")
    public Query getQuery() {
        return query;
    }

} // class GeneratedBy
