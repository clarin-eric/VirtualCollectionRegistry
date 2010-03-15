package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "resource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "type", "ref" })
public class Resource {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false, updatable = false, insertable = true)
	private long id = -1;
	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.ORDINAL)
	private ResourceType type;
	@Column(name = "ref", nullable = false)
	private String ref;

	private Resource() {
		super();
	}

	public Resource(ResourceType type, String ref) {
		this();
		this.setType(type);
		this.setRef(ref);
	}

	public long getId() {
		return id;
	}

	@XmlID
	@XmlAttribute(name = "id")
	public String getIdForXml() {
		return "r" + id;
	}

	public void setType(ResourceType type) {
		if (type == null) {
			throw new NullPointerException("type == null");
		}
		this.type = type;
	}

	@XmlElement(name = "ResourceType")
	public ResourceType getType() {
		return type;
	}

	public void setRef(String ref) {
		if (ref == null) {
			throw new NullPointerException("ref == null");
		}
		this.ref = ref;
	}

	@XmlElement(name = "ResourceRef")
	public String getRef() {
		return ref;
	}

	protected int getSignature() {
		return new HashCodeBuilder(799, 51)
			.append(type)
			.append(ref)
			.toHashCode();
	}

} // class Resource
