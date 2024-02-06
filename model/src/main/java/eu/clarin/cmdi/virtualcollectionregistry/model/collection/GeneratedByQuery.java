package eu.clarin.cmdi.virtualcollectionregistry.model.collection;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Embeddable
public class GeneratedByQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "generatedby_query_profile", length = 255)
    private String profile;

    //@Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "generatedby_query_value", length = 8192)
    private String value;


    public GeneratedByQuery() {
        super();
    }

    public GeneratedByQuery(String profile, String value) {
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
        if (obj instanceof GeneratedByQuery) {
            final GeneratedByQuery rhs = (GeneratedByQuery) obj;
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