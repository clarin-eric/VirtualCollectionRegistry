package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@DiscriminatorValue("M")
@XmlRootElement(name = "ResourceMetadata")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(namespace = "urn:x-vcr:virtualcollection:resourcemetadata",
         propOrder = { "name", "description", "creator", "ref" })
@XmlSeeAlso({ Resource.class, Creator.class })
public class ResourceMetadata extends Resource {
	@Column(name = "name")
	private String name;
	@Column(name = "description")
	private String description;
	@Embedded
	private Creator creator;
	@Column(name = "ref", nullable = false)
	private String ref;
	@Column(name = "pid")
	private String pid;

	@SuppressWarnings("unused")
	private ResourceMetadata() {
		super();
	}
	
	public ResourceMetadata(String title, String description, String ref) {
		super();
		this.setName(title);
		this.setDescriptione(description);
		this.setRef(ref);
	}

	public ResourceType getType() {
		return ResourceType.METADATA;
	}
	
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		this.name = name;
	}
	
	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}

	public void setDescriptione(String description) {
		this.description = description;
	}
	
	@XmlElement(name = "Description")
	public String getDescription() {
		return description;
	}

	public void setCreator(Creator creator) {
		this.creator = creator;
	}

	@XmlElement(name = "Creator")
	public Creator getCreator() {
		return creator;
	}

	public void setRef(String ref) {
		if (ref == null) {
			throw new IllegalArgumentException("ref == null");
		}
		this.ref = ref;
	}
	
	@XmlElement(name = "ResourceRef")
	public String getRef() {
		return ref;
	}
	
	public void setPid(String pid) {
		if (pid == null) {
			throw new IllegalArgumentException("pid == null");
		}
		this.pid = pid;
	}
	
	public String getPid() {
		return pid;
	}

	int getSignature() {
		return new HashCodeBuilder(859, 83)
			.append(name)
			.append(description)
			.append((creator != null) ? creator.getSignature() : null)
			.append(ref)
			.toHashCode();
	}

} // class ResourceMetadata
