package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@Table(name = "pid")
public class PersistentIdentifier {
    public static enum Type {
        DUMMY, GWDG;
    } // public enum Type

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id = -1;
    @OneToOne(fetch = FetchType.EAGER,
              optional = false)
    private VirtualCollection collection;
    @Column(name = "type")
    @Enumerated(EnumType.ORDINAL)
    private Type type;
    @Column(name = "identifier",
            nullable = false,
            updatable = false,
            unique = true)
    private String identifier;
    @Column(name = "last_modified",
            nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Version
    private Date lastModifed;

    private PersistentIdentifier() {
    }

    PersistentIdentifier(VirtualCollection collection, Type type,
            String identifier) {
        this();
        if (collection == null) {
            throw new NullPointerException("collection == null");
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
        this.collection = collection;
        this.type = type;
        this.identifier = identifier;
    }

    public long getId() {
        return id;
    }

    public VirtualCollection getVirtualCollection() {
        return collection;
    }

    public Type getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Date getLastModified() {
        return lastModifed;
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
