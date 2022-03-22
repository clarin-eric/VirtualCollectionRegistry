package eu.clarin.cmdi.virtualcollectionregistry.model;

import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifier;
import eu.clarin.cmdi.wicket.components.citation.Citable;
import eu.clarin.cmdi.wicket.components.pid.PersistentIdentifieable;
import eu.clarin.cmdi.wicket.components.pid.PidType;
import java.io.Serializable;
import java.util.*;

import javax.persistence.Basic;
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
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    query = "SELECT COUNT(c) "+
                            "FROM VirtualCollection c " +
                            "WHERE "+
                                "c.state = eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection$State.PUBLIC"+
                                " OR "+
                                "c.state = eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection$State.PUBLIC_FROZEN"),
        @NamedQuery(name = "VirtualCollection.findAllPublicOrigins",
                    query = "SELECT DISTINCT(c.origin) "+
                           "FROM VirtualCollection c " +
                            "WHERE ("+
                                "c.state = eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection$State.PUBLIC" +
                                " OR " +
                                "c.state = eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection$State.PUBLIC_FROZEN" +
                                ") AND " +
                                "c.origin IS NOT NULL"),
        @NamedQuery(name = "VirtualCollection.findByOwner",
                    query = "SELECT c FROM VirtualCollection c " +
                            "WHERE c.owner = :owner ORDER BY c.id"),
        @NamedQuery(name = "VirtualCollection.countByOwner",
                    query = "SELECT COUNT(c) FROM VirtualCollection c " +
                            "WHERE c.owner = :owner"),
        @NamedQuery(name = "VirtualCollection.findAllByState",
                    query = "SELECT c FROM VirtualCollection c " +
                            "WHERE c.state = :state AND c.dateModified < :date"),
        @NamedQuery(name = "VirtualCollection.findAllByStates",
                    query = "SELECT c FROM VirtualCollection c " +
                            "WHERE c.state IN :states AND c.dateModified < :date")
})
public class VirtualCollection implements Serializable, IdentifiedEntity, PersistentIdentifieable, Citable {
    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollection.class);

    public static final Type DEFAULT_TYPE_VALUE = Type.EXTENSIONAL;
    public static final Purpose DEFAULT_PURPOSE_VALUE = Purpose.REFERENCE;
    public static final Reproducibility DEFAULT_REPRODUCIBILIY_VALUE = Reproducibility.INTENDED;

    public Set<PersistentIdentifier> getIdentifiers() {
        if(identifiers == null) {
            return new HashSet<>();
        }
        return identifiers;
    }
    
    @Override
    public String getIdentifier() {
        if(!hasPersistentIdentifier()) {
            return null;
        }
        PersistentIdentifier primaryId = getPrimaryIdentifier();
        if(primaryId == null) {
            return null;
        }
        return primaryId.getIdentifier();
    }

    @Override
    public String getPidUri() {
        if(!hasPersistentIdentifier()) {
            return null;
        }
        PersistentIdentifier primaryId = getPrimaryIdentifier();
        if(primaryId == null) {
            return null;
        }
        return primaryId.getActionableURI();
    }

    public PersistentIdentifier getPrimaryIdentifier() {
        for (PersistentIdentifier id : getIdentifiers()) {
            if (id.getPrimary()) {
                return id;
            }
        }
        return null;
    }
    
    @Override
    public PidType getPidType() {
        if(!hasPersistentIdentifier()) {
            return null;
        }
        PersistentIdentifier primaryId = getPrimaryIdentifier();
        if(primaryId == null) {
            return null;
        }
        return HandleLinkModel.getPidType(primaryId.getURI());
    }

    @Override
    public String getPidTitle() {
        if(!hasPersistentIdentifier()) {
            return null;
        }
        return getIdentifier();
    }

    @Override
    public List<String> getAuthors() {
        final Set<String> authors = new HashSet<>();
        //authors.add(getOwner().getName());
        for(Creator c : getCreators()) {
            authors.add(c.getPerson());
        }
        return new ArrayList<>(authors);
    }

    @Override
    public String getYear() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(getCreationDate());
        int year = calendar.get(Calendar.YEAR);
        return String.valueOf(year);
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getUri() {
        //TODO: Handle non PID case? Can this happen?
        return getPidUri();
    }

    public String getProblemDetails() {
        return problemDetails;
    }

    public void setProblemDetails(String problemDetails) {
        this.problemDetails = problemDetails;
    }

    public Date getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(Date datePublished) {
        this.datePublished = datePublished;
    }

    public static enum State {
        PRIVATE,
        PUBLIC_PENDING,
        PUBLIC,
        PUBLIC_FROZEN_PENDING,
        PUBLIC_FROZEN,
        DELETED,
        DEAD,
        ERROR
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

    public static enum Problem {
        PID_MINTING_HTTP_ERROR,
        PID_MINTING_UNKOWN,
        UNKOWN
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(cascade = { CascadeType.PERSIST,
                           CascadeType.REFRESH,
                           CascadeType.MERGE,
                           CascadeType.DETACH
    },
               fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id",
                nullable = false)
    private User owner;

    /*
    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.EAGER,
              mappedBy = "vc",
              optional = true)
    private PersistentIdentifier persistentId = null;
    */
    
    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.EAGER,
                mappedBy = "vc")
    private Set<PersistentIdentifier> identifiers;

    //Make this a list of problems?

    /* Indication of the issue if state = ERROR */
    @Column(name = "problem", nullable = true)
    private VirtualCollection.Problem problem;

    @Column(name = "problem_details", nullable = true)
    private String problemDetails;

    @Column(name = "origin", nullable = true)
    private String origin;

    @Column(name = "state", nullable = false)
    private VirtualCollection.State state;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type", nullable = false)
    private VirtualCollection.Type type;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    //@Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "description", length = 8192)
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

    //@Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "reproducibility_notice", length = 8192)
    private String reproducibilityNotice;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "keyword",
                     joinColumns = @JoinColumn(name="vc_id"))
    private List<String> keywords;

    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @JoinColumn(name = "vc_id", nullable = false)    
    @OrderColumn(nullable = false)
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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "published", nullable = true)
    private Date datePublished;

    public VirtualCollection() {
        super();
        this.setState(VirtualCollection.State.PRIVATE);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
/*
    public PersistentIdentifier getPersistentIdentifier() {
        
        return persistentId;
    }
  */  
    @Override
    public boolean hasPersistentIdentifier() {
        PersistentIdentifier primaryId = getPrimaryIdentifier();
        return primaryId != null;
    }

    public void setPersistentIdentifier(PersistentIdentifier persistentId) {
        if (persistentId == null) {
            throw new NullPointerException("pid == null");
        }
        if (persistentId.getPrimary() && hasPersistentIdentifier()) {
            throw new IllegalStateException("Already has a primary peristent identifier");
        }
        if(!(state == State.PUBLIC_PENDING || state == State.PUBLIC_FROZEN_PENDING || state == State.ERROR)) {
            throw new IllegalStateException("illegal state, current state = "+state);
        }

        if(this.identifiers == null) {
            this.identifiers = new HashSet<>();
        }
        this.identifiers.add(persistentId);

        /*
        switch(state) {
            case PUBLIC_PENDING: this.state = State.PUBLIC; break;
            case PUBLIC_FROZEN_PENDING: this.state = State.PUBLIC_FROZEN; break;
            case ERROR: this.state = State.PUBLIC; break; //TODO: properly handle switching from ERROR to PUBLIC_FROZEN state
            default: throw new IllegalStateException("Invalid state transition. Unexpected source state: "+state);
        }
         */
    }

    public VirtualCollection.Problem getProblem() {
        return problem;
    }

    public void setProblem(VirtualCollection.Problem problem) {
        this.problem = problem;
    }
    
    public State getState() {
        return state;
    }

    public void setState(State state) {
        if (state == null) {
            throw new NullPointerException("state == null");
        }
        this.state = state;
        this.setDateModified(new Date());
    }

    public boolean isPrivate() {
        return state == State.PRIVATE;
    }

    public boolean isPublic() {
        return (state == State.PUBLIC_PENDING) || (state == State.PUBLIC);
    }

    public boolean isPublicFrozen() {
        return (state == State.PUBLIC_FROZEN_PENDING) || (state == State.PUBLIC_FROZEN) ;
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
        Collections.sort(this.creators);
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

        //The persistent identifiers should not be changed.
        //Add check to make sure they are equal.

        this.setState(state);
        this.setType(vc.getType());
        this.setName(vc.getName());
        this.setDescription(vc.getDescription());
        this.setCreationDate(vc.getCreationDate());
        this.setPurpose(vc.getPurpose());
        this.setReproducibility(vc.getReproducibility());
        this.setReproducibilityNotice(vc.getReproducibilityNotice());

        /*
        // Clear any removed creators
        Set<Creator> obsolete_creators = new HashSet<Creator>(this.getCreators());
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
        */
        this.creators = vc.getCreators();

        /*
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
        */
        this.keywords = vc.getKeywords();

        /*
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
        */
        this.resources = vc.getResources();

        if (vc.generatedBy != null) {
            final GeneratedBy genBy = vc.generatedBy;
            if (this.generatedBy == null) {
                this.generatedBy = new GeneratedBy(genBy.getDescription());
            } else {
                this.generatedBy.setDescription(genBy.getDescription());
            }
            this.generatedBy.setURI(genBy.getURI());
            if (genBy.getQuery() != null) {
                final GeneratedByQuery q = genBy.getQuery();
                GeneratedByQuery query =
                    new GeneratedByQuery(q.getProfile(), q.getValue());
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
                .append(this.getIdentifiers(),
                            rhs.getIdentifiers())
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
            .append(this.getPrimaryIdentifier())
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
    
    public boolean hasCreator(Creator creator) {
        for(Creator c: this.creators) {
            if(c.getPerson().equalsIgnoreCase(creator.getPerson())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Is this collection citaeable? 
     * In order to be citeable the collection must be published and have a persistent identifier.
     * @return 
     */
    public boolean isCiteable() {
        return hasPersistentIdentifier() && (getState() == VirtualCollection.State.PUBLIC || getState() == VirtualCollection.State.PUBLIC_FROZEN);
    }
    
    @Override
    public String toString() {
        String result = "";
        result += String.format("id             : %s\n", this.getId());
        result += String.format("pid            : %s\n", this.getPrimaryIdentifier());
        result += String.format("name           : %s\n", this.getName());
        result += String.format("owner          : %s\n", this.getOwner());
        result += String.format("purpose        : %s\n", this.getPurpose());
        result += String.format("reproducibility: %s\n", this.getReproducibility());
        result += String.format("repro, notice  : %s\n", this.getReproducibilityNotice());
        result += String.format("creators       :\n");
        for(Creator c : this.getCreators()) { 
            result += String.format("  creator      : %s\n", this.getReproducibilityNotice());
        }
        result += String.format("keywords       :\n");
        for(String keyword: this.getKeywords()) {
            result += String.format("  keyword      : %s\n", this.getReproducibilityNotice());
        }
        result += String.format("resources       :\n");
        for(Resource c : this.getResources()) {
            result += String.format("  resources    : %s\n", this.getReproducibilityNotice());
        }
        return result;
    }

    public boolean canMerge() {
        return  state != State.ERROR &&
                state != VirtualCollection.State.DELETED &&
                state != VirtualCollection.State.DEAD &&
                state != VirtualCollection.State.PUBLIC_FROZEN &&
                state != VirtualCollection.State.PUBLIC_FROZEN_PENDING &&
                state != VirtualCollection.State.PUBLIC_PENDING &&
                state != VirtualCollection.State.PUBLIC;
    }

    /**
     * Merge resources from the otherCollection with this collection.
     *
     * @param otherCollection
     * @return
     */
    public void merge(VirtualCollection otherCollection) {
        //Add merged resources to this collection
        for(Resource r : otherCollection.getResources()) {
            boolean exists = false;
            for(Resource existing_resource : getResources()) {
                if(existing_resource.getRef().equalsIgnoreCase(r.getRef())) {
                    exists = true;
                }
            }

            if(!exists) {
                Resource new_r = new Resource();
                new_r.setLabel(r.getLabel());
                new_r.setDescription(r.getDescription());
                new_r.setMimetype(r.getMimetype());
                new_r.setRef(r.getRef());
                new_r.setType(r.getType());
                new_r.setMerged();
                new_r.setOrigin(r.getOrigin());
                new_r.setOriginalQuery(r.getOriginalQuery());
                getResources().add(new_r);
            } else {
                logger.warn("Skipping resource with duplicate ref: "+r.getRef());
            }
        }
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

} // class VirtualCollection
