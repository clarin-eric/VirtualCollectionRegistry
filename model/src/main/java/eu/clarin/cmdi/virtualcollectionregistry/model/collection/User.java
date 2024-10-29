package eu.clarin.cmdi.virtualcollectionregistry.model.collection;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.ApiKey;
import java.io.Serializable;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "vcr_user")
@NamedQueries({
    @NamedQuery(name = "User.findByName",
                query = "SELECT u FROM User u WHERE u.name = :name")
})
public class User implements Serializable, IdentifiedEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @OneToMany(
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        mappedBy = "owner",
        orphanRemoval = true
    )
    private Set<VirtualCollection> collections = new LinkedHashSet<VirtualCollection>();

    @OneToMany(
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        mappedBy = "user",
        orphanRemoval = true
    )
    private Set<ApiKey> apiKeys = new LinkedHashSet<ApiKey>();

    protected User() { }

    public User(String name, String displayName) {
        super();
        this.setName(name);
        this.setDisplayName(displayName);
    }

    public User(Principal principal) {
        this(principal == null ? "unknown" : principal.getName());
    }

    public User(String name) {
        this(name, null);
    }

    public Long getId() {
        return id;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (displayName != null) {
            displayName = displayName.trim();
            if (displayName.isEmpty()) {
                displayName = null;
            }
        }
        this.displayName = displayName;
    }

    public Set<VirtualCollection> getVirtualCollections() {
        return collections;
    }

    public boolean equalsPrincipal(Principal principal) {
        if (principal == null) {
            throw new NullPointerException("principal == null");
    }
        return name.equals(principal.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof User) {
            final User rhs = (User) obj;
            return new EqualsBuilder()
                .append(this.getName(), rhs.getName())
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(22391, 295)
            .append(this.getName())
            .toHashCode();
    }

    public Set<ApiKey> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(Set<ApiKey> apiKeys) {
        this.apiKeys = apiKeys;
    }
} // class User
