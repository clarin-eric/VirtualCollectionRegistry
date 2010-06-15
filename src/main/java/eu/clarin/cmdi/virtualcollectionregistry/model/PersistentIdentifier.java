package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.apache.commons.lang.NullArgumentException;

@Entity
@Table(name = "pid")
public class PersistentIdentifier {
    public static enum Type {
        DUMMY, GWDG;
    } // public enum Type

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id = -1;
    @OneToOne(optional = false)
    @PrimaryKeyJoinColumn
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

    // XXX: rename to getActionableURI()?
    public String createURI() {
        switch (type) {
        case DUMMY:
            return identifier;
        case GWDG:
            return "http://hdl.handle.net/" + identifier;
        default:
            throw new InternalError();
        }
    }

} // class PersistentIdentifier
