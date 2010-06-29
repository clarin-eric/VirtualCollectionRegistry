package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.virtualcollectionregistry.model.mapper.DateAdapter;

@Entity
@Table(name = "virtual_collection")
@NamedQueries({
        @NamedQuery(name = "VirtualCollection.byUUID",
                    query = "SELECT c FROM VirtualCollection c " +
                            "WHERE c.name = :uuid"),
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
                            "WHERE c.state = :state AND c.modifedDate < :date")
})
@XmlRootElement(name = "VirtualCollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "name", "description", "creationDate", "type",
                       "creator", "purpose", "reproducibility",
                       "reproducibilityNotice", "resources", "generatedBy" })
@XmlSeeAlso({ Creator.class,
              GeneratedBy.class,
              Resource.class,
              PersistentIdentifier.class })
public class VirtualCollection {
    @XmlType(namespace = "urn:x-vcr:virtualcollection:state")
    @XmlEnum(String.class)
    public static enum State {
        @XmlEnumValue("private")
        PRIVATE,
        @XmlEnumValue("public-pending")
        PUBLIC_PENDING,
        @XmlEnumValue("public")
        PUBLIC,
        @XmlEnumValue("deleted")
        DELETED,
        @XmlEnumValue("dead")
        DEAD
    } // enum State
    @XmlType(namespace = "urn:x-vcr:virtualcollection:type")
    @XmlEnum(String.class)
    public static enum Type {
        @XmlEnumValue("extensional")
        EXTENSIONAL,
        @XmlEnumValue("intensional")
        INTENSIONAL
    }
    @XmlType(namespace = "urn:x-vcr:virtualcollection:purpose")
    @XmlEnum(String.class)
    public static enum Purpose {
        @XmlEnumValue("research")
        RESEARCH,
        @XmlEnumValue("reference")
        REFERENCE,
        @XmlEnumValue("sample")
        SAMPLE,
        @XmlEnumValue("future-use")
        FUTURE_USE
    }
    @XmlType(namespace = "urn:x-vcr:virtualcollection:reproducability")
    @XmlEnum(String.class)
    public static enum Reproducibility {
        @XmlEnumValue("intended")
        INTENDED,
        @XmlEnumValue("fluctuating")
        FLUCTUATING,
        @XmlEnumValue("untended")
        UNTENDED
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id = -1;
    @ManyToOne(cascade = { CascadeType.PERSIST,
                           CascadeType.REFRESH,
                           CascadeType.MERGE },
               fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id",
                nullable = false)
    private User owner;
    @Column(name = "state",
            nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private State state = State.PRIVATE;
    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.EAGER,
              mappedBy = "vc",
              optional = true)
    private PersistentIdentifier pid;
    @Column(name = "name",
            nullable = false)
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "creation_date")
    @Temporal(TemporalType.DATE)
    private Date creationDate;
    @Column(name = "type")
    @Enumerated(EnumType.ORDINAL)
    private Type type = Type.EXTENSIONAL;
    @Embedded
    private Creator creator;
    @Column(name = "purpose")
    @Enumerated(EnumType.ORDINAL)
    private Purpose purpose;
    @Column(name = "reproducibility")
    @Enumerated(EnumType.ORDINAL)
    private Reproducibility reproducibility;
    @Column(name = "reproducibility_notice")
    private String reproducibilityNotice;
    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @JoinColumn(name = "vc_id",
                nullable = false)
    @OrderBy("id")
    private Set<Resource> resources = new LinkedHashSet<Resource>();
    @Embedded
    private GeneratedBy generatedby;
    @Column(name = "created",
            nullable = false,
            updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date();
    @Column(name = "modified",
            nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Version
    private Date modifedDate;

    @XmlAttribute(name = "id")
    public long getId() {
        return id;
    }

    public void setState(State state) {
        if (state == null) {
            throw new NullPointerException("state == null");
        }
        this.state = state;
    }

    @XmlAttribute(name = "state")
    public State getState() {
        return state;
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

    public void setOwner(User owner) {
        if (owner == null) {
            throw new NullPointerException("owner == null");
        }
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }

    public void setPersistentIdentifier(PersistentIdentifier pid) {
        if (pid == null) {
            throw new NullPointerException("pid == null");
        }
        if ((this.pid != null) || (state != State.PUBLIC_PENDING)) {
            throw new IllegalStateException("illegal state");
        }
        this.pid = pid;
        this.state = State.PUBLIC;
    }

    public PersistentIdentifier getPersistentIdentifier() {
        return pid;
    }

    @XmlAttribute(name = "persistentId")
    public String getPersistentIdentifierForXml() {
        if (pid != null) {
            return pid.getIdentifier();
        }
        return null;
    }

    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        this.name = name;
    }

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "Description")
    public String getDescription() {
        return description;
    }

    public void setCreationDate(Date creationDate) {
        if (creationDate == null) {
            throw new NullPointerException("creationDate == null");
        }
        this.creationDate = creationDate;
    }

    @XmlElement(name = "CreationDate")
    @XmlJavaTypeAdapter(DateAdapter.class)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setType(Type style) {
        if (style == null) {
            throw new NullPointerException("style == null");
        }
        this.type = style;
    }

    @XmlElement(name = "Type")
    public Type getType() {
        return type;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    @XmlElement(name = "Creator")
    public Creator getCreator() {
        return creator;
    }

    public void setPurpose(Purpose purpose) {
        this.purpose = purpose;
    }
    
    @XmlElement(name = "Purpose")
    public Purpose getPurpose() {
        return purpose;
    }
    
    public void setReproducibility(Reproducibility reproducibility) {
        this.reproducibility = reproducibility;
    }

    @XmlElement(name = "Reproducability")
    public Reproducibility getReproducibility() {
        return reproducibility;
    }

    public void setReproducibilityNotice(String reproducibilityNotice) {
        this.reproducibilityNotice = reproducibilityNotice;
    }

    @XmlElement(name = "ReproducibilityNotice")
    public String getReproducibilityNotice() {
        return reproducibilityNotice;
    }

    @XmlElementWrapper(name = "Resources")
    @XmlElements({ @XmlElement(name = "Resource", type = Resource.class) })
    public Set<Resource> getResources() {
        return resources;
    }

    public void setGeneratedBy(GeneratedBy generatedby) {
        this.generatedby = generatedby;
    }

    @XmlElement(name = "GeneratedBy")
    public GeneratedBy getGeneratedBy() {
        return generatedby;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        if (modifiedDate == null) {
            throw new NullPointerException("modifiedDate == null");
        }
        this.modifedDate = modifiedDate;
    }

    public Date getModifiedDate() {
        return modifedDate;
    }

    public void updateFrom(VirtualCollection vc) {
        if (this == vc) {
            return;
        }
        this.setState(state);
        this.setName(vc.getName());
        if (vc.getPersistentIdentifier() != null) {
            this.setPersistentIdentifier(vc.getPersistentIdentifier());
        }
        this.setDescription(vc.getDescription());
        this.setCreationDate(vc.getCreationDate());
        this.setType(vc.getType());
        Creator c = vc.getCreator();
        if (c != null) {
            this.creator.setName(c.getName());
            this.creator.setEMail(c.getEMail());
            this.creator.setOrganisation(c.getOrganisation());
        } else {
            this.creator = null;
        }
        this.setPurpose(vc.getPurpose());
        this.setReproducibility(vc.getReproducibility());
        this.setReproducibilityNotice(vc.getReproducibilityNotice());

        // add all new resources to set
        this.resources.addAll(vc.resources);
        // purge all deleted members
        this.resources.retainAll(vc.resources);
    }

} // class VirtualCollection
