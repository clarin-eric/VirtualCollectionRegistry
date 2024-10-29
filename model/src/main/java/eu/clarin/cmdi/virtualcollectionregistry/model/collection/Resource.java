package eu.clarin.cmdi.virtualcollectionregistry.model.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifieable;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PidLink;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PidType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.CascadeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "resource")
public class Resource implements Serializable, IdentifiedEntity, PersistentIdentifieable, Orderable, Comparable {
    
    private final static Logger logger = LoggerFactory.getLogger(Resource.class);
    private static final long serialVersionUID = 1L;

    private transient boolean merged = false;

    public static enum Type {
        METADATA,
        RESOURCE;
    } // enum Resource.Type

    @XmlElement(name = "mimeType")
    @JsonProperty(value = "mimeType")
    @Column(name = "mimetype", nullable = true, length = 255)
    private String mimeType;

    /**
     * @return the mimeType
     */
    public String getMimetype() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimetype(String mimeType) {
        if(mimeType != null) {
            if (mimeType.equalsIgnoreCase("application/x-cmdi+xml")) {
                setType(Resource.Type.METADATA);
            } else {
                setType(Resource.Type.RESOURCE);
            }
        }
        this.mimeType = mimeType;
    }

    @XmlElement(name = "check")
    @JsonProperty(value = "check")
    @Column(name = "checked", nullable = true, length = 255)
    private String check;

    /**
     * @return the check
     */
    public String getCheck() {
        return check;
    }

    /**
     * @param check the check to set
     */
    public void setCheck(String check) {
        this.check = check;
    }

    @XmlElement(name = "id")
    @JsonProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @XmlElement(name = "type")
    @JsonProperty(value = "type")
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Type type;
    
    @XmlElement(name = "ref")
    @JsonProperty(value = "ref")
    @Column(name = "ref", nullable = false, length = 255)
    private String ref;
    
    @XmlElement(name = "label")
    @JsonProperty(value = "label")
    @Column(name = "label", nullable = true, length = 255)
    private String label;
    
    //@Lob
    @XmlElement(name = "description")
    @JsonProperty(value = "description")
    @Column(name = "description", length = 8192)
    private String description;

    @XmlElement(name = "displayOrder")
    @JsonProperty(value = "displayOrder")
    @Column(name = "display_order", nullable = false)
    private Long displayOrder;

    @XmlElement(name = "origin")
    @JsonProperty(value = "origin")
    @Column(name = "origin", nullable = true)
    private String origin;

    @XmlElement(name = "originalQuery")
    @JsonProperty(value = "originalQuery")
    @Column(name = "original_query", nullable = true)
    private String originalQuery;

    @XmlElement(name = "kvs")
    @JsonProperty(value = "kvs")
    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.EAGER,
               mappedBy = "resource")
    private Set<ResourceKv> kvs;
    
    public Resource() {
        super();
        this.displayOrder = 0L;
    }
    
    public Resource(Resource.Type type, String ref) {
        super();
        this.displayOrder = 0L;
        this.setType(type);
        this.setRef(ref);
    }

    public Resource(Resource.Type type, String ref, String label) {
        super();
        this.displayOrder = 0L;
        this.setType(type);
        this.setRef(ref);
        this.setLabel(label);
    }

    @Override
    public Long getId() {
        return id;
    }
    
    public Resource.Type getType() {
        return type;
    }
    
    public void setType(Resource.Type type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        this.type = type;
    }
    
    public String getRef() {
        return ref;
    }
    
    public void setRef(String ref) {
        if (ref == null) {
            throw new NullPointerException("ref == null");
        }
        ref = ref.trim();
        if (ref.isEmpty()) {
            throw new IllegalArgumentException("ref is empty");
        }
        this.ref = ref;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Resource) {
            final Resource rhs = (Resource) obj;
            return new EqualsBuilder()
                    .append(this.getType(), rhs.getType())
                    .append(this.getRef(), rhs.getRef())
                    .isEquals();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(25973, 1815)
                .append(this.getType())
                .append(this.getRef())
                .toHashCode();
    }
    
    public void valuesFrom(Resource resource) {
        ref = resource.getRef();
        type = resource.getType();
        label = resource.getLabel();
        description = resource.getDescription();
    }

    /**
     * Return a new resource with all fields, including the id deep copied
     * @return
     */
    public Resource clone() {
        final Resource copy = fork();
        copy.id = getId();
        return copy;
    }

    /**
     * Return a new resource with all fields except the id deep copied
     * @return
     */
    public Resource fork() {
        final Resource copy = new Resource();
        copy.setMimetype(getMimetype());
        copy.setCheck(getCheck());
        copy.setType(getType());
        copy.setRef(getRef());
        copy.setLabel(getLabel());
        copy.setDescription(getDescription());
        copy.setDisplayOrder(getDisplayOrder());
        copy.setOrigin(getOrigin());
        copy.setOriginalQuery(getOriginalQuery());
        if(merged) {
            copy.setMerged();
        }
        return copy;
    }
    
    
    @Override
    public String getIdentifier() {
        if(!hasPersistentIdentifier()) {
            return null;
        }
        
        String pid = null;
        switch(getPidType()) {
            case HANDLE: pid = PidLink.getHandleIdentifier(this.ref); break;
            case DOI: pid = PidLink.getDoiIdentifier(this.ref); break;
        }
        return pid;
    }

    @Override
    public String getPidUri() {
        if(!hasPersistentIdentifier()) {
            return null;
        }
        
        return PidLink.getActionableUri(this.ref);
        //return this.ref;
    }

    @Override
    public PidType getPidType() {
        if(!hasPersistentIdentifier()) {
            return null;
        }       
        
        return PidLink.getPidType(this.ref);
    }

    @Override
    public String getPidTitle() {
        if(!hasPersistentIdentifier()) {
            return null;
        }
        
        return this.ref;
    }

    @Override
    public boolean hasPersistentIdentifier() {
        if(this.ref == null) {
            return false;
        }
        return  PidLink.isHandle(this.ref) || 
                PidLink.isDoi(this.ref) || 
                PidLink.isNbn(this.ref);
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
        if(o instanceof Resource) {
            return OrderableComparator.compare(this, (Resource)o);
        }
        return 0;
    }

    public void setMerged() {
        this.merged = true;
    }

    public boolean isMerged() {
        return this.merged;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }

    public void setValuesFrom(Resource other) { }

    /**
     * @return the kvs
     */
    public Set<ResourceKv> getKvs() {
        return kvs;
    }

    /**
     * @param kvs the kvs to set
     */
    public void setKvs(Set<ResourceKv> kvs) {
        this.kvs = kvs;
    }

} // class Resource
