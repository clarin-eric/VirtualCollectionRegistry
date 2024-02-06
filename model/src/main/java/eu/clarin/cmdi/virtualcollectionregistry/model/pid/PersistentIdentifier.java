package eu.clarin.cmdi.virtualcollectionregistry.model.pid;


import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import java.io.Serializable;
import javax.persistence.*;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "pid")
public class PersistentIdentifier implements PersistentIdentifieable, Serializable {

    private static final long serialVersionUID = 1L;

    public static enum Type {
        DUMMY, HANDLE, DOI;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL,
          fetch = FetchType.LAZY,
          optional = true)
    @JoinColumn(name = "vc_id",
            nullable = false,
            unique = false)
    private VirtualCollection vc;

    @Column(name = "type")
    @Enumerated(EnumType.ORDINAL)
    private Type type;

    @Column(name = "identifier",
            nullable = false,
            unique = true,
            length = 255)
    private String identifier;

    @Column(name = "is_primary", columnDefinition = "TINYINT", length = 1)
    private Boolean primary;

    @Column(name = "is_latest", columnDefinition = "TINYINT", length = 1)
    private Boolean latest;

    protected PersistentIdentifier() { }

    public PersistentIdentifier(VirtualCollection vc, Type type, String identifier) {
        this(vc, type, true, false, identifier);
    }

    public PersistentIdentifier(VirtualCollection vc, Type type, boolean primary, String identifier) {
        this(vc, type, primary, false, identifier);
    }

    public PersistentIdentifier(VirtualCollection vc, Type type, boolean primary, boolean latest, String identifier) {
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
        this.primary = primary;
        this.latest = latest;
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

    @Override
    public String getPidUri() {
        return getActionableURI();
    }

    @Override
    public PidType getPidType() {
        return PidLink.getPidType(getURI());
    }

    @Override
    public String getPidTitle() {
        return getIdentifier();
    }

    @Override
    public boolean hasPersistentIdentifier() {
        return true;
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    public Boolean getLatest() {
        return latest;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    public void setVirtualCollection(VirtualCollection vc) {
        this.vc = vc;
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
            case DOI:
                return "doi:" + identifier;
            default:
                throw new InternalError();
        }
    }

    /**
     * Provides a URI that can be used, without modification, to "act on" this
     * present identifier (as if clicked on in a standard web browser).
     * <em>Actionable URI</em> is defined as follows: "URI (3.2.2) that has a
     * resource-associated identifier (3.2.1) that is suitably encoded, such
     * that when the URI is embedded in a web document and 'clicked' on, the
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
            case DOI:
                return "https://doi.org/" + identifier;
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
