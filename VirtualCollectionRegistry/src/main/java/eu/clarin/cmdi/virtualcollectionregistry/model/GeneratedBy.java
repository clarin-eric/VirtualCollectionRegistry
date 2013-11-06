package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Embeddable
public class GeneratedBy implements Serializable {
    private static final long serialVersionUID = 1L;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "generatedby_description", length = 8192)
    private String description;

    @Column(name = "generatedby_uri", nullable = true, length = 255)
    private String uri;

    @Embedded
    private GeneratedByQuery query;

    public GeneratedBy() {
        super();
    }

    public GeneratedBy(String description) {
        super();
        this.setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new NullPointerException("description == null");
        }
        description = description.trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("description is empty");
        }
        this.description = description;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        if (uri != null) {
            uri = uri.trim();
            if (uri.isEmpty()) {
                uri = null;
            }
        }
        this.uri = uri;
    }

    public GeneratedByQuery getQuery() {
        return query;
    }

    public void setQuery(GeneratedByQuery query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof GeneratedBy) {
            final GeneratedBy rhs = (GeneratedBy) obj;
            return new EqualsBuilder()
                .append(this.getDescription(), rhs.getDescription())
                .append(this.getURI(), rhs.getURI())
                .append(this.getQuery(), rhs.getQuery())
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7587, 16231)
            .append(this.getDescription())
            .append(uri)
            .append(query)
            .toHashCode();
    }

} // class GeneratedBy
