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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Cascade;

import eu.clarin.cmdi.virtualcollectionregistry.model.mapper.DateAdapter;

@Entity
@Table(name = "virtual_collection")
@NamedQueries({
	@NamedQuery(name = "VirtualCollection.findAll",
			    query = "SELECT c FROM VirtualCollection c")
})
@XmlRootElement(name = "VirtualCollection")
@XmlAccessorType(XmlAccessType.FIELD)
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
	@XmlAttribute(name = "id")
	private long id;
	@ManyToOne(cascade = { CascadeType.PERSIST,
						   CascadeType.REFRESH,
                           CascadeType.MERGE },
               fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_id", nullable = false)
	@XmlTransient
	private User owner;
	@Column(name = "pid", nullable = false)
	@XmlAttribute(name = "persistentId")
	private String pid;
	@Column(name = "name", nullable = false)
	@XmlElement(name = "Name")
	private String name;
	@Column(name = "description")
	@XmlElement(name = "Description")
	private String description;
	@Column(name = "creation_date")
	@Temporal(TemporalType.DATE)
	@XmlElement(name = "CreationDate")
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date creationDate;
	@Column(name = "visibility")
	@Enumerated(EnumType.ORDINAL)
	@XmlElement(name = "Visibility")
	private Visibility visibility = Visibility.ADVERTISED;
	@Column(name = "type")
	@Enumerated(EnumType.ORDINAL)
	@XmlElement(name = "Type")
	private Type type = Type.EXTENSIONAL;
	@Column(name = "origin")
	@XmlElement(name = "Origin")
	private String origin;
	@Embedded
	@XmlElement(name = "Creator")
	private Creator creator;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "vc_id", nullable = false)
    // FIXME: maybe use OrderColumn when migrating to JPA 2.0
	@OrderBy("id")
	// FIXME: is there something alike in JPA 2.0?
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@XmlElementWrapper(name = "Resources")
	@XmlElements({ @XmlElement(name = "ResourceProxy",
                               type = ResourceProxy.class),
                   @XmlElement(name = "ResourceMetadata",
                		       type = ResourceMetadata.class) })
	private Set<Resource> resources = new LinkedHashSet<Resource>();
	@Column(name = "created", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@XmlTransient
	private Date createdDate = new Date();
	@Column(name = "modified", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Version
	@XmlTransient
	private Date modifedDate;

	public long getId() {
		return id;
	}

	public void setOwner(User owner) {
		if (owner == null) {
			throw new IllegalArgumentException("owner == null");
		}
		this.owner = owner;
	}

	public User getOwner() {
		return owner;
	}

	public void setPid(String pid) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		this.pid = pid;
	}
	
	public String getPid() {
		return pid;
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

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setCreationDate(Date creationDate) {
		if (creationDate == null) {
			throw new IllegalArgumentException("creationDate == null");
		}
		this.creationDate = creationDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setVisibility(Visibility visibility) {
		if (visibility == null) {
			throw new IllegalArgumentException("visibility == null");
		}
		this.visibility = visibility;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setType(Type style) {
		if (style == null) {
			throw new IllegalArgumentException("style == null");
		}
		this.type = style;
	}

	public Type getType() {
		return type;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getOrigin() {
		return origin;
	}

	public void setCreator(Creator creator) {
		this.creator = creator;
	}

	public Creator getCreator() {
		return creator;
	}

	public Set<Resource> getResources() {
		return resources;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		if (modifiedDate == null) {
			throw new IllegalArgumentException("modifiedDate == null");
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
		this.creator.setName(vc.getCreator().getName());
		this.creator.setEMail(vc.getCreator().getEMail());
		this.creator.setOrganisation(vc.getCreator().getOrganisation());

		HashMap<Integer, Resource> old_res =
			new HashMap<Integer, Resource>(this.resources.size());
		for(Resource r : this.resources) {
			old_res.put(r.getSignature(), r);
			
		}
		HashMap<Integer, Resource> new_res =
			new HashMap<Integer, Resource>(vc.getResources().size());
		for(Resource r : vc.getResources()) {
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
