package eu.clarin.cmdi.virtualcollectionregistry.model;

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

@Entity
@Table(name = "user")
@NamedQueries({
	@NamedQuery(name = "User.findByName",
				query = "SELECT u FROM User u WHERE u.name = :name")
})
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@OneToMany(mappedBy = "owner",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
	private Set<VirtualCollection> collections =
		new LinkedHashSet<VirtualCollection>();

	@SuppressWarnings("unused")
	private User() {
	}
	
	public User(String name) {
		super();
		this.setName(name);
	}

	public long getId() {
		return id;
	}
	
	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException("name == null");
		}
		name = name.trim();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("empty name is not allowed");
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public Set<VirtualCollection> getVirtualCollections() {
		return collections;
	}

	public boolean equals(Object o) {
		if (o == null) {
			throw new NullPointerException("o == null");
		}
		if (o instanceof User) {
			return (name.equals(((User) o).getName()) ||
					id == ((User) o).getId()); 
		}
		return false;
	}
	
	public boolean equalsPrincipal(Principal principal) {
		if (principal == null) {
			throw new NullPointerException("principal == null");
		}
		return name.equals(principal.getName()); 
	}

} // class User
