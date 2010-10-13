package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "virtualcollection")
@NamedQueries({
        @NamedQuery(name = "VirtualCollection.findAllPublic",
                    query = "SELECT c FROM VirtualCollection c " +
                            "WHERE c.state = eu.clarin.cmdi." +
                            "virtualcollectionregistry.model." +
                            "VirtualCollection$State.PUBLIC " +
                            "ORDER BY c.id"),
        @NamedQuery(name = "VirtualCollection.countAllPublic",
                    query = "SELECT COUNT(c) FROM VirtualCollection c " +
                            "WHERE c.state = eu.clarin.cmdi." +
                            "virtualcollectionregistry.model." +
                            "VirtualCollection$State.PUBLIC"),
        @NamedQuery(name = "VirtualCollection.findByOwner",
                    query = "SELECT c FROM VirtualCollection c " +
                            "WHERE c.owner = :owner ORDER BY c.id"),
        @NamedQuery(name = "VirtualCollection.countByOwner",
                    query = "SELECT COUNT(c) FROM VirtualCollection c " +
                            "WHERE c.owner = :owner"),
        @NamedQuery(name = "VirtualCollection.findAllByState",
                    query = "SELECT c FROM VirtualCollection c " +
                            "WHERE c.state = :state AND c.modifiedDate < :date")
})
public class VirtualCollection implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum State {
        PRIVATE,
        PUBLIC_PENDING,
        PUBLIC,
        DELETED,
        DEAD
    } // enum VirtualCollection.State

    public static enum Type {
        EXTENSIONAL,
        INTENSIONAL
    } // enum VirtualCollecion.Type

    public static enum Purpose {
        RESEARCH,
        REFERENCE,
        SAMPLE,
        FUTURE_USE
    } // enum VirtualCollecion.Purpose

    public static enum Reproducibility {
        INTENDED,
        FLUCTUATING,
        UNTENDED
    } // enum VirtualCollecion.Reproducibility

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private long id;

    @ManyToOne(cascade = { CascadeType.PERSIST,
                           CascadeType.REFRESH,
                           CascadeType.MERGE },
               fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id",
                nullable = false)
    private User owner;

    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.EAGER,
              mappedBy = "vc",
              optional = true)
    private PersistentIdentifier persistentId = null;

    @Column(name = "state", nullable = false)
    private VirtualCollection.State state;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type", nullable = false)
    private VirtualCollection.Type type;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(name = "creation_date")
    private Date creationDate;

    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @JoinColumn(name = "vc_id", nullable = false)
    private List<Creator> creators = new ArrayList<Creator>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "purpose")
    private VirtualCollection.Purpose purpose;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "reproducibility")
    private VirtualCollection.Reproducibility reproducibility;
    
    @Column(name = "reproducibility_notice")
    private String reproducibilityNotice;

    @ElementCollection
    @CollectionTable(name = "keyword",
                     joinColumns = @JoinColumn(name="vc_id"))
    private List<String> keywords = new ArrayList<String>();

    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @JoinColumn(name = "vc_id", nullable = false)
    private List<Resource> resources = new ArrayList<Resource>();

    @Embedded
    private GeneratedBy generatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false, updatable = false)
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Version
    @Column(name = "modified", nullable = false)
    private Date modifiedDate;

    @SuppressWarnings("unused")
    private VirtualCollection() {
    }
    
    public VirtualCollection(VirtualCollection.Type type, String name) {
        super();
        this.setState(VirtualCollection.State.PRIVATE);
        this.setType(type);
        this.setName(name);
    }

    public long getId() {
        return id;
    }

    protected void setId(long value) {
        this.id = value;
    }

    public User getOwner() {
        return owner;
    }
    
    public void setOwner(User owner) {
        if (owner == null) {
            throw new NullPointerException("owner == null");
        }
        this.owner = owner;
    }

    public PersistentIdentifier getPersistentIdentifier() {
        return persistentId;
    }

    public void setPersistentIdentifier(PersistentIdentifier persistentId) {
        if (persistentId == null) {
            throw new NullPointerException("pid == null");
        }
        if ((this.persistentId != null) || (state != State.PUBLIC_PENDING)) {
            throw new IllegalStateException("illegal state");
        }
        this.persistentId = persistentId;
        this.state = State.PUBLIC;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        if (state == null) {
            throw new NullPointerException("state == null");
        }
        this.state = state;
    }

    public boolean isPrivate() {
        return state == State.PRIVATE;
    }

    public boolean isPublic() {
        return (state == State.PUBLIC_PENDING) || (state == State.PUBLIC); 
    }

    public boolean isDeleted() {
        return (state == State.DELETED) || (state == State.DEAD); 
    }

    public VirtualCollection.Type getType() {
        return type;
    }

    public void setType(VirtualCollection.Type type) {
        this.type = type;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<Creator> getCreators() {
        return creators;
    }

    public void setCreators(List<Creator> creators) {
        this.creators = creators;
    }

    public VirtualCollection.Purpose getPurpose() {
        return purpose;
    }

    public void setPurpose(VirtualCollection.Purpose purpose) {
        this.purpose = purpose;
    }

    public VirtualCollection.Reproducibility getReproducibility() {
        return reproducibility;
    }

    public void setReproducibility(
            VirtualCollection.Reproducibility reproducibility) {
        this.reproducibility = reproducibility;
    }

    public String getReproducibilityNotice() {
        return reproducibilityNotice;
    }

    public void setReproducibilityNotice(String reproducibilityNotice) {
        this.reproducibilityNotice = reproducibilityNotice;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeyworks(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> value) {
        this.resources = value;
    }

    public GeneratedBy getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(GeneratedBy generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        if (modifiedDate == null) {
            throw new NullPointerException("modifiedDate == null");
        }
        this.modifiedDate = modifiedDate;
    }

    public void updateFrom(VirtualCollection vc) {
        if (this == vc) {
            return;
        }
        if (vc.getPersistentIdentifier() != null) {
            this.setPersistentIdentifier(vc.getPersistentIdentifier());
        }
        this.setState(state);
        this.setType(vc.getType());
        this.setName(vc.getName());
        this.setDescription(vc.getDescription());
        this.setCreationDate(vc.getCreationDate());
        if ((vc.creators != null) && !vc.creators.isEmpty()) {
            this.creators.clear();
            this.creators.addAll(vc.creators);
//            // add all new creators
//            this.creators.addAll(vc.creators);
//            // purge all deleted creators
//            this.creators.retainAll(vc.creators);
        } else {
            this.creators.clear();
        }
        this.setPurpose(vc.getPurpose());
        this.setReproducibility(vc.getReproducibility());
        this.setReproducibilityNotice(vc.getReproducibilityNotice());
        if ((vc.keywords != null) && !vc.keywords.isEmpty()) {
            this.keywords.clear();
            this.keywords.addAll(vc.keywords);
//            // add all new resources
//            this.keywords.addAll(vc.keywords);
//            // purge all deleted keywords
//            this.keywords.retainAll(vc.keywords);
        }
        if ((vc.resources != null) && !vc.resources.isEmpty()) {
            this.resources.clear();
            this.resources.addAll(vc.resources);
//            // add all new resources
//            this.resources.addAll(vc.resources);
//            // purge all deleted resources
//            this.resources.retainAll(vc.resources);
        } else {
            this.resources.clear();
        }
        if (vc.generatedBy != null) {
            final GeneratedBy genBy = vc.generatedBy;
            if (this.generatedBy == null) {
                this.generatedBy = new GeneratedBy(genBy.getDescription());
            } else {
                this.generatedBy.setDescription(genBy.getDescription());
            }
            this.generatedBy.setURI(genBy.getURI());
            if (genBy.getQuery() != null) {
                final GeneratedBy.Query q = genBy.getQuery();
                GeneratedBy.Query query =
                    new GeneratedBy.Query(q.getProfile(), q.getValue());
                this.generatedBy.setQuery(query);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof VirtualCollection) {
            final VirtualCollection rhs = (VirtualCollection) obj;
            return new EqualsBuilder()
                .append(owner, rhs.owner)
                .append(persistentId, rhs.persistentId)
                .append(state, rhs.state)
                .append(type, rhs.type)
                .append(name, rhs.name)
                .append(description, rhs.description)
                .append(creationDate, rhs.creationDate)
                .append(creators, rhs.creators)
                .append(purpose, rhs.purpose)
                .append(reproducibility, rhs.reproducibility)
                .append(reproducibilityNotice, rhs.reproducibilityNotice)
                .append(keywords, rhs.keywords)
                .append(generatedBy, rhs.generatedBy)
//                .append(createdDate, rhs.createdDate)
//                .append(modifiedDate, rhs.modifiedDate)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1391, 295)
            .append(owner)
            .append(persistentId)
            .append(state)
            .append(type)
            .append(name)
            .append(description)
            .append(creationDate)
            .append(creators)
            .append(purpose)
            .append(reproducibility)
            .append(reproducibilityNotice)
            .append(keywords)
            .append(resources)
            .append(generatedBy)
//            .append(createdDate)
//            .append(modifiedDate)
            .toHashCode();
    }

} // class VirtualCollection
