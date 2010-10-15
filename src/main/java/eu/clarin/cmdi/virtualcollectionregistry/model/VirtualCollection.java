package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.persistence.OrderBy;
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
                            "WHERE c.state = :state AND c.dateModified < :date")
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
    private Long id;

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
    @OrderBy("id")
    private List<Creator> creators;

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
    private List<String> keywords;

    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @JoinColumn(name = "vc_id", nullable = false)
    @OrderBy("id")
    private List<Resource> resources;

    @Embedded
    private GeneratedBy generatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false, updatable = false)
    private Date dateCreated = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Version
    @Column(name = "modified", nullable = false)
    private Date dateModified;

    public VirtualCollection() {
        super();
        this.setState(VirtualCollection.State.PRIVATE);
    }
    
    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }
    
    public void setOwner(User owner) {
        if (owner == null) {
            throw new NullPointerException("owner == null");
        }
        if (this.owner != null) {
            this.owner.getVirtualCollections().remove(this);
        }
        this.owner = owner;
        this.owner.getVirtualCollections().add(this);
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
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<Creator> getCreators() {
        if (creators == null) {
            this.creators = new ArrayList<Creator>();
        }
        return creators;
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
        if (reproducibilityNotice != null) {
            reproducibilityNotice = reproducibilityNotice.trim();
            if (reproducibilityNotice.isEmpty()) {
                reproducibilityNotice = null;
            }
        }
        this.reproducibilityNotice = reproducibilityNotice;
    }

    public List<String> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<String>();
        }
        return keywords;
    }

    public List<Resource> getResources() {
        if (resources == null) {
            resources = new ArrayList<Resource>();
        }
        return resources;
    }

    public GeneratedBy getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(GeneratedBy generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        if (dateModified == null) {
            throw new NullPointerException("dateModified == null");
        }
        this.dateModified = dateModified;
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
        
        // Creators
        Set<Creator> obsolete_creators =
            new HashSet<Creator>(this.getCreators());
        for (Creator creator : vc.getCreators()) {
            if (!obsolete_creators.contains(creator)) {
                this.getCreators().add(creator);
            }
            obsolete_creators.remove(creator);
        }
        if (!obsolete_creators.isEmpty()) {
            for (Creator creator : obsolete_creators) {
                this.getCreators().remove(creator);
            }
            obsolete_creators = null;
        }

        this.setPurpose(vc.getPurpose());
        this.setReproducibility(vc.getReproducibility());
        this.setReproducibilityNotice(vc.getReproducibilityNotice());

        // Keywords
        Set<String> obsolete_keywords =
            new HashSet<String>(this.getKeywords());
        for (String keyword : vc.getKeywords()) {
            if (!obsolete_keywords.contains(keyword)) {
                this.getKeywords().add(keyword);
            }
            obsolete_keywords.remove(keyword);
        }
        if (!obsolete_keywords.isEmpty()) {
            for (String keyword : obsolete_keywords) {
                this.getKeywords().remove(keyword);
            }
            obsolete_keywords = null;
        }

        // Resources
        Set<Resource> obsolete_resources =
            new HashSet<Resource>(this.getResources());
        for (Resource resource : vc.getResources()) {
            if (!obsolete_resources.contains(resource)) {
                this.getResources().add(resource);
            }
            obsolete_resources.remove(resource);
        }
        if (!obsolete_resources.isEmpty()) {
            for (Resource resource : obsolete_resources) {
                this.getResources().remove(resource);
            }
            obsolete_resources = null;
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
                .append(this.getOwner(), rhs.getOwner())
                .append(this.getPersistentIdentifier(),
                            rhs.getPersistentIdentifier())
                .append(this.getState(), rhs.getState())
                .append(this.getType(), rhs.getType())
                .append(this.getName(), rhs.getName())
                .append(this.getDescription(), rhs.getDescription())
                .append(this.getCreationDate(), rhs.getCreationDate())
                .append(this.getCreators(), rhs.getCreators())
                .append(this.getPurpose(), rhs.getPurpose())
                .append(this.getReproducibility(), rhs.getReproducibility())
                .append(this.getReproducibilityNotice(),
                            rhs.getReproducibilityNotice())
                .append(this.getKeywords(), rhs.getKeywords())
                .append(this.getResources(), rhs.getResources())
                .append(this.getGeneratedBy(), rhs.getGeneratedBy())
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1391, 295)
            .append(this.getOwner())
            .append(this.getPersistentIdentifier())
            .append(this.getState())
            .append(this.getType())
            .append(this.getName())
            .append(this.getDescription())
            .append(this.getCreationDate())
            .append(this.getCreators())
            .append(this.getPurpose())
            .append(this.getReproducibility())
            .append(this.getReproducibilityNotice())
            .append(this.getKeywords())
            .append(this.getResources())
            .append(this.getGeneratedBy())
            .toHashCode();
    }

} // class VirtualCollection
