package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "pid")
public class PersistentIdentifier implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum Type {
        DUMMY, GWDG;
    } // public enum Type

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.LAZY,
              optional = true)
    @JoinColumn(name="vc_id",
                nullable = false,
                unique = true)
    private VirtualCollection vc;

    @Column(name = "type")
    @Enumerated(EnumType.ORDINAL)
    private Type type;

    @Column(name = "identifier",
            nullable = false,
            unique = true,
            length = 255)
    private String identifier;

    
    private PersistentIdentifier() {
    }

    PersistentIdentifier(VirtualCollection vc, Type type, String identifier) {
        this();
        if (vc == null) {
            throw new NullArgumentException("vc == null");
        }
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        if (identifier == null) {
            throw new NullPointerException("identifier == null");
        }
        identifier = identifier.trim();
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("identifier is empty");
        }
        this.vc = vc;
        this.type = type;
        this.identifier = identifier;
    }

    public Long getId() {
        return id;
    }

    public VirtualCollection getVirtualCollection() {
        return vc;
    }

    public Type getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getActionableURI() {
        switch (type) {
        case DUMMY:
            return "dummy:identifier";
        case GWDG:
            return "http://hdl.handle.net/" + identifier;
        default:
            throw new InternalError();
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
        if (obj instanceof PersistentIdentifier) {
            final PersistentIdentifier rhs = (PersistentIdentifier) obj;
            return new EqualsBuilder()
                .append(this.getVirtualCollection(), rhs.getVirtualCollection())
                .append(this.getType(), rhs.getType())
                .append(this.getIdentifier(), rhs.getIdentifier())
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(26249, 4651)
            .append(this.getVirtualCollection())
            .append(this.getType())
            .append(this.getIdentifier())
            .toHashCode();
    }

} // class PersistentIdentifier
