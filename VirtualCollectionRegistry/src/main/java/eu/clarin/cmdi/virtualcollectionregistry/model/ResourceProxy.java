package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "resource_proxy")
@DiscriminatorValue("P")
@XmlRootElement(name = "ResourceProxy")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(namespace = "urn:x-vcr:virtualcollection:resourceproxy",
         propOrder = { "type", "ref" })
@XmlSeeAlso({ Resource.class })
public class ResourceProxy extends Resource {
	@Column(name = "type")
	@Enumerated(EnumType.ORDINAL)
	private ResourceType type = ResourceType.METADATA;
	@Column(name = "ref", nullable = false)
	private String ref;

	@SuppressWarnings("unused")
	private ResourceProxy() {
		super();
	}

	public ResourceProxy(ResourceType type, String ref) {
		super();
		this.setType(type);
		this.setRef(ref);
	}

	public void setType(ResourceType type) {
		if (type == null) {
			throw new IllegalArgumentException("type == null");
		}
		this.type = type;
	}

	@XmlElement(name = "ResourceType")
	public ResourceType getType() {
		return type;
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

	int getSignature() {
		return new HashCodeBuilder(799, 51)
			.append(type)
			.append(ref)
			.toHashCode();
	}

} // class ResourceProxy
