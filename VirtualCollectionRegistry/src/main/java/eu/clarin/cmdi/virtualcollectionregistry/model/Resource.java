package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "resource")
public class Resource implements Serializable, IdentifiedEntity {
    
    private static final long serialVersionUID = 1L;
    
    public static enum Type {
        
        METADATA,
        RESOURCE;
    } // enum Resource.Type

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Type type;
    
    @Column(name = "ref", nullable = false, length = 255)
    private String ref;
    
    @Column(name = "label", nullable = true, length = 255)
    private String label;
    
    @Lob
    @Column(name = "description", length = 8192)
    private String description;
    
    public Resource() {
        super();
    }
    
    public Resource(Resource.Type type, String ref) {
        super();
        this.setType(type);
        this.setRef(ref);
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
    
    public Resource getCopy() {
        final Resource copy = new Resource();
        copy.id = id;
        copy.setRef(ref);
        copy.setType(type);
        copy.setLabel(label);
        copy.setDescription(description);
        return copy;
    }
    
} // class Resource
