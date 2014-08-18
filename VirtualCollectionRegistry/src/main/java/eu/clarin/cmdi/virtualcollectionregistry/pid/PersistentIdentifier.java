package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
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

        DUMMY, HANDLE;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = true)
    @JoinColumn(name = "vc_id",
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

    protected PersistentIdentifier() {
    }

    public PersistentIdentifier(VirtualCollection vc, Type type, String identifier) {
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

    /**
     * Provides a URI representation of this persistent identifier. Notice that
     * this URI is not necessarily actionable, and may therefore differ from the
     * result of {@link #getActionableURI() }
     *
     * @return a URI for this identifier
     * @see #getActionableURI()
     */
    public String getURI() {
        switch (type) {
            case DUMMY:
                return "dummy:identifier-" + vc.getId();
            case HANDLE:
                return "hdl:" + identifier;
            default:
                throw new InternalError();
        }
    }

    /**
     * Provides a URI that can be used, without modification, to "act on" this
     * present identifier (as if clicked on in a standard web browser).
     * <em>Actionable URI</em> is defined as follows: "URI (3.2.2) that has a
     * resource-associated identifier (3.2.1) that is suitably encoded, such
     * that when the URI is embedded in a web document and “clicked” on, the
     * browser will be redirected to the resource (3.1.1), and possibly
     * supplementary services related to the resource" (ISO 24619:2011 (PISA),
     * section 3.2.6.)
     *
     * @return a string representation of an actionable URI for this identifier
     * @see ISO 24619:2011 (PISA), section 3.2.6
     */
    public String getActionableURI() {
        switch (type) {
            case DUMMY:
                return "dummy:identifier-" + vc.getId();
            case HANDLE:
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
                    .append(this.getType(), rhs.getType())
                    .append(this.getIdentifier(), rhs.getIdentifier())
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(26249, 4651)
                .append(this.getType())
                .append(this.getIdentifier())
                .toHashCode();
    }

} // class PersistentIdentifier
