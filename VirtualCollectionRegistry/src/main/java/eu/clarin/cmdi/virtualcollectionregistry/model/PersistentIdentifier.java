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
    @Column(name = "id")
    private long id;

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
            unique = true)
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

    public long getId() {
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
                .appendSuper(super.equals(obj))
                .append(vc, rhs.vc)
                .append(type, rhs.type)
                .append(identifier, rhs.identifier)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(26249, 4651)
            .appendSuper(super.hashCode())
            .append(vc)
            .append(type)
            .append(identifier)
            .toHashCode();
    }

} // class PersistentIdentifier
