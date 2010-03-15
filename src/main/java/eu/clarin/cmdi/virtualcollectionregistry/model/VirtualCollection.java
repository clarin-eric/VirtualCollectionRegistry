package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.virtualcollectionregistry.model.mapper.DateAdapter;

@Entity
@Table(name = "virtual_collection")
@NamedQueries({
	@NamedQuery(name = "VirtualCollection.findAll",
				query = "SELECT c FROM VirtualCollection c"),
    @NamedQuery(name = "VirtualCollection.countAll",
    			query = "SELECT COUNT(c) FROM VirtualCollection c"),
    @NamedQuery(name = "VirtualCollection.findByOwner",
    			query = "SELECT c FROM VirtualCollection c " +
    			        "WHERE c.owner = :owner"),
	@NamedQuery(name = "VirtualCollection.countByOwner",
    			query = "SELECT COUNT(c) FROM VirtualCollection c " +
    			        "WHERE c.owner = :owner")
})
@XmlRootElement(name = "VirtualCollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "name", "description", "creationDate", "visibility",
		"type", "origin", "creator", "resources" })
@XmlSeeAlso({ Creator.class, Resource.class })
public class VirtualCollection {
	@XmlType(namespace = "urn:x-vcr:virtualcollection:visibility")
	@XmlEnum(String.class)
	public static enum Visibility {
		@XmlEnumValue("advertised")
		ADVERTISED,
		@XmlEnumValue("non-advertised")
		NON_ADVERTISED;
	} // enum Visibility

	@XmlType(namespace = "urn:x-vcr:virtualcollection:type")
	@XmlEnum(String.class)
	public static enum Type {
		@XmlEnumValue("extensional")
		EXTENSIONAL,
		@XmlEnumValue("intensional")
		INTENSIONAL
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id = -1;
	@ManyToOne(cascade = { CascadeType.PERSIST,
						   CascadeType.REFRESH,
                           CascadeType.MERGE },
               fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
	@Column(name = "pid", nullable = false)
	private String pid;
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "description")
	private String description;
	@Column(name = "creation_date")
	@Temporal(TemporalType.DATE)
	private Date creationDate;
	@Column(name = "visibility")
	@Enumerated(EnumType.ORDINAL)
	private Visibility visibility = Visibility.ADVERTISED;
	@Column(name = "type")
	@Enumerated(EnumType.ORDINAL)
	private Type type = Type.EXTENSIONAL;
	@Column(name = "origin")
	private String origin;
	@Embedded
	private Creator creator;
	@OneToMany(cascade = CascadeType.ALL,
			   fetch = FetchType.LAZY,
			   orphanRemoval = true)
	@JoinColumn(name = "vc_id", nullable = false)
	@OrderBy("id")
	private Set<Resource> resources = new LinkedHashSet<Resource>();
	@Column(name = "created", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate = new Date();
	@Column(name = "modified", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Version
	private Date modifedDate;

	@XmlAttribute(name = "id")
	public long getId() {
		return id;
	}

	public void setOwner(User owner) {
		if (owner == null) {
			throw new NullPointerException("owner == null");
		}
		this.owner = owner;
	}

	public User getOwner() {
		return owner;
	}

	public void setPid(String pid) {
		if (name == null) {
			throw new NullPointerException("name == null");
		}
		pid = pid.trim();
		if (pid.length() < 1) {
			throw new IllegalArgumentException("empty pid is not allowed");
		}
		this.pid = pid;
	}
	
	@XmlAttribute(name = "persistentId")
	public String getPid() {
		return pid;
	}

	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException("name == null");
		}
		this.name = name;
	}

	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name = "Description")
	public String getDescription() {
		return description;
	}

	public void setCreationDate(Date creationDate) {
		if (creationDate == null) {
			throw new NullPointerException("creationDate == null");
		}
		this.creationDate = creationDate;
	}

	@XmlElement(name = "CreationDate")
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getCreationDate() {
		return creationDate;
	}

	public void setVisibility(Visibility visibility) {
		if (visibility == null) {
			throw new NullPointerException("visibility == null");
		}
		this.visibility = visibility;
	}

	@XmlElement(name = "Visibility")
	public Visibility getVisibility() {
		return visibility;
	}

	public void setType(Type style) {
		if (style == null) {
			throw new NullPointerException("style == null");
		}
		this.type = style;
	}

	@XmlElement(name = "Type")
	public Type getType() {
		return type;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	@XmlElement(name = "Origin")
	public String getOrigin() {
		return origin;
	}

	public void setCreator(Creator creator) {
		this.creator = creator;
	}

	@XmlElement(name = "Creator")
	public Creator getCreator() {
		return creator;
	}

	@XmlElementWrapper(name = "Resources")
	@XmlElements({ @XmlElement(name = "Resource",
                               type = Resource.class) })
	public Set<Resource> getResources() {
		return resources;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		if (modifiedDate == null) {
			throw new NullPointerException("modifiedDate == null");
		}
		this.modifedDate = modifiedDate;
	}
	
	public Date getModifiedDate() {
		return modifedDate;
	}

	public void updateFrom(VirtualCollection vc) {
		if (this == vc) {
			return;
		}
		this.setName(vc.getName());
		this.setDescription(vc.getDescription());
		this.setCreationDate(vc.getCreationDate());
		this.setVisibility(vc.getVisibility());
		this.setType(vc.getType());
		this.setOrigin(vc.getOrigin());
		Creator c = vc.getCreator();
		if (c != null) {
			this.creator.setName(c.getName());
			this.creator.setEMail(c.getEMail());
			this.creator.setOrganisation(c.getOrganisation());
		} else {
			this.creator = null;
		}

		HashMap<Integer, Resource> old_res =
			new HashMap<Integer, Resource>(this.resources.size());
		for (Resource r : this.resources) {
			old_res.put(r.getSignature(), r);

		}
		HashMap<Integer, Resource> new_res =
			new HashMap<Integer, Resource>(vc.getResources().size());
		for (Resource r : vc.getResources()) {
			new_res.put(r.getSignature(), r);
		}
		for (Resource r : new_res.values()) {
			if (!old_res.containsKey(r.getSignature())) {
				resources.add(r);
			}
		}
		for (Resource r : old_res.values()) {
			if (!new_res.containsKey(r.getSignature())) {
				resources.remove(r);
			}
		}
	}

} // class VirtualCollection
