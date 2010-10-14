package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.io.Serializable;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "user")
@NamedQueries({
    @NamedQuery(name = "User.findByName",
                query = "SELECT u FROM User u WHERE u.name = :name")
})
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "display_name")
    private String displayName;
    
    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               mappedBy = "owner",
               orphanRemoval = true)
    private Set<VirtualCollection> collections =
        new LinkedHashSet<VirtualCollection>();

    
    @SuppressWarnings("unused")
    private User() {
    }

    public User(String name, String displayName) {
        super();
        this.setName(name);
        this.setDisplayName(displayName);
    }

    public User(String name) {
        this(name, null);
    }

    public Long getId() {
        return id;
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
        return name;
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

    public String getName() {
        return displayName;
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

} // class User
