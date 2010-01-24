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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
               cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // FIXME: proprietary, JPA 2.0 provides orphanRemoval, but it's yet available
    @OnDelete(action=OnDeleteAction.CASCADE)               
	private Set<VirtualCollection> collections = new LinkedHashSet<VirtualCollection>();

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
			throw new IllegalArgumentException("name == null");
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
			throw new IllegalArgumentException("o == null");
		}
		if (o instanceof User) {
			return (name.equals(((User) o).getName()) ||
					id == ((User) o).getId()); 
		}
		return false;
	}
	
	public boolean equalsPrincipal(Principal principal) {
		if (principal == null) {
			throw new IllegalArgumentException("principal == null");
		}
		return name.equals(principal.getName()); 
	}

} // class USer
