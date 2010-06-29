package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "description", "query" },
         namespace = "urn:x-vcr:generatedby")
public class GeneratedBy {
    @Column(name = "generatedby_description")
    private String description;
    @Column(name = "generatedby_query")
    private String query;

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

    public void setQuery(String query) {
        this.query = query;
    }

    @XmlElement(name = "Query")
    public String getQuery() {
        return query;
    }

} // class GeneratedBy
