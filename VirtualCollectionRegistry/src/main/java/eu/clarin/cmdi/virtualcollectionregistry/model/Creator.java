package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.HashCodeBuilder;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "name", "EMail", "organisation" },
		 namespace = "urn:x-vcr:creator")
public class Creator {
	@Column(name = "creator_name")
	private String name;
	@Column(name = "creator_email")
	private String email;
	@Column(name = "creator_organisation")
	private String organisation;
	
	@SuppressWarnings("unused")
	private Creator() {
		this(null, null, null);
	}

	public Creator(String name) {
		this(name, null, null);
	}

	public Creator(String name, String email) {
		this(name, email, null);
	}

	public Creator(String name, String email, String organization) {
		super();
		this.setName(name);
		this.setEMail(email);
		this.setOrganisation(organization);
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}

	public void setEMail(String email) {
		this.email = email;
	}

	@XmlElement(name = "Email")
	public String getEMail() {
		return email;
	}

	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}

	@XmlElement(name = "Organisation")
	public String getOrganisation() {
		return organisation;
	}

	int getSignature() {
		return new HashCodeBuilder(469, 41)
			.append(name)
			.append(email)
			.append(organisation)
			.toHashCode();

	}

} // class Creator
