package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "creator")
public class Creator implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private long id;
    
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "organisation")
    private String organisation;

    
    @SuppressWarnings("unused")
    private Creator() {
    }

    public Creator(String name, String email, String organisation) {
        super();
        this.setName(name);
        this.setEMail(email);
        this.setOrganisation(organisation);
    }
    
    public Creator(String name) {
        this(name, null, null);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getEMail() {
        return email;
    }

    public void setEMail(String value) {
        this.email = value;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String value) {
        this.organisation = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Creator) {
            final Creator rhs = (Creator) obj;
            return new EqualsBuilder()
                .append(name, rhs.name)
                .append(email, rhs.email)
                .append(organisation, rhs.organisation)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(32361, 7611)
            .append(name)
            .append(email)
            .append(organisation)
            .toHashCode();
    }

} // class Creator
