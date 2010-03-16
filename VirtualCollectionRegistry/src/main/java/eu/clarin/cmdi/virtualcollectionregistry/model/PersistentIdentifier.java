package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.net.URI;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@Table(name = "pid")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.CHAR, name = "type")
@DiscriminatorValue("_")
public abstract class PersistentIdentifier {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id = -1;
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	protected VirtualCollection collection;
	@Column(name = "identifier", nullable = false, updatable = false)
	protected String identifier;
	@Column(name = "last_modified", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Version
	private Date lastModifed;

	protected PersistentIdentifier() {
	}

	protected PersistentIdentifier(VirtualCollection collection,
			                       String identifier) {
		if (collection == null) {
			throw new NullPointerException("collection == null");
		}
		if (identifier == null) {
			throw new NullPointerException("identifier == null");
		}
		identifier = identifier.trim();
		if (identifier.isEmpty()) {
			throw new IllegalArgumentException("identifier is empty");
		}
		this.collection = collection;
		this.identifier = identifier;
	}

	public long getId() {
		return id;
	}

	public VirtualCollection getVirtualCollection() {
		return collection;
	}

	public String getIdentifier() {
		return identifier;
	}

	public Date getLastModified() {
		return lastModifed;
	}

	public abstract URI createURI();

} // class PersistentIdentifier
