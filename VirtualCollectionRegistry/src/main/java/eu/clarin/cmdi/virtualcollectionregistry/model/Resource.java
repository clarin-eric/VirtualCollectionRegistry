package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;

@Entity
@Table(name = "resource")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType=DiscriminatorType.STRING, length=1)
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({ ResourceProxy.class, ResourceMetadata.class })
public abstract class Resource {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false, updatable = false, insertable = true)
	private long id = -1;

	protected Resource() {
		super();
	}

	public long getId() {
		return id;
	}

	@XmlID
	@XmlAttribute(name = "id")
	public String getIdForXml() {
		return "r" + id;
	}
	
	public abstract ResourceType getType();

	public abstract String getRef();

	abstract int getSignature();

} // abstract class Resource
