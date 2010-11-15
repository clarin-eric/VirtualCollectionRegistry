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

    @Embeddable
    public static class Query implements Serializable {
        private static final long serialVersionUID = 1L;

        @Column(name = "generatedby_query_profile", length = 255)
        private String profile;

        @Lob
        @Basic(fetch = FetchType.EAGER)
        @Column(name = "generatedby_query_value", length = 8192)
        private String value;

        
        public Query() {
            super();
        }

        public Query(String profile, String value) {
            super();
            this.setProfile(profile);
            this.setValue(value);
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            if (profile == null) {
                throw new NullPointerException("profile == null");
            }
            profile = profile.trim();
            if (profile.isEmpty()) {
                throw new IllegalArgumentException("profile is empty");
            }
            this.profile = profile;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            if (value == null) {
                throw new NullPointerException("value == null");
            }
            // do not trim and allow space only values
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof GeneratedBy.Query) {
                final GeneratedBy.Query rhs = (GeneratedBy.Query) obj;
                return new EqualsBuilder()
                    .append(profile, rhs.profile)
                    .append(value, rhs.value)
                    .isEquals();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(10021, 32555)
                .append(profile)
                .append(value)
                .toHashCode();
        }

    } // class GeneratedBy.Query

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "generatedby_description", length = 8192)
    private String description;

    @Column(name = "generatedby_uri", nullable = true, length = 255)
    private String uri;

    @Embedded
    private GeneratedBy.Query query;

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

    public GeneratedBy.Query getQuery() {
        return query;
    }

    public void setQuery(GeneratedBy.Query query) {
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
