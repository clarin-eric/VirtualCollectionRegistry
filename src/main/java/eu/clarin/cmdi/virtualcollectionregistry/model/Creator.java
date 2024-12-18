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
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "creator")
public class Creator implements Serializable, IdentifiedEntity, Orderable, Comparable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "family_name", nullable = false, length = 255)
    private String familyName;

    @Column(name = "given_name", nullable = false, length = 255)
    private String givenName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "organisation", length = 255)
    private String organisation;

    @Column(name = "telephone", length = 255)
    private String telephone;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "role", length = 255)
    private String role;

    @Column(name = "display_order", nullable = false)
    private Long displayOrder;

    public Creator() {
        super();
        this.displayOrder = 0L;
    }

    public Creator(String familyName, String givenName) {
        super();
        this.displayOrder = 0L;
        this.setFamilyName(familyName);
        this.setGivenName(givenName);
    }


    @Override
    public Long getId() {
        return id;
    }

    public String getPerson() {
        if(getGivenName() == null) {
            return getFamilyName();
        }
        return getFamilyName() +", "+ getGivenName();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEMail() {
        return email;
    }

    public void setEMail(String email) {
        this.email = email;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
                    .append(this.getPerson(), rhs.getPerson())
                    .append(this.getAddress(), rhs.getAddress())
                    .append(this.getEMail(), rhs.getEMail())
                    .append(this.getOrganisation(), rhs.getOrganisation())
                    .append(this.getTelephone(), rhs.getTelephone())
                    .append(this.getWebsite(), rhs.getWebsite())
                    .append(this.getRole(), rhs.getRole())
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(32361, 7611)
                .append(this.getPerson())
                .append(this.getAddress())
                .append(this.getEMail())
                .append(this.getOrganisation())
                .append(this.getTelephone())
                .append(this.getWebsite())
                .append(this.getRole())
                .toHashCode();
    }
    
    public void setValuesFrom(Creator other) {
        address = other.getAddress();
        email = other.getEMail();
        organisation = other.getOrganisation();
        givenName = other.getGivenName();
        familyName = other.getFamilyName();
        role = other.getRole();
        telephone = other.getTelephone();
        website = other.getWebsite();
    }

    /**
     * Creates a copy 
     *
     * @return a clone of this creator
     */
    public Creator getCopy() {
        final Creator copy = new Creator();
        copy.id = id;
        copy.setAddress(address);
        copy.setEMail(email);
        copy.setOrganisation(organisation);
        copy.setFamilyName(familyName);
        copy.setGivenName(givenName);
        copy.setRole(role);
        copy.setTelephone(telephone);
        copy.setWebsite(website);
        return copy;
    }

    @Override
    public Long getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if( o == null) return 0;
        if(o instanceof Creator) {
            return OrderableComparator.compare(this, (Creator)o);
        }
        return 0;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    
    @Override
    public String toString() {
        return getFamilyName()+" "+getGivenName()+", email: "+getEMail()+", org: "+getOrganisation();
    }
} // class Creator
