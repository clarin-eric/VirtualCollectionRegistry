package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "handle")
@NamedQueries({
	@NamedQuery(name = "Handle.findByPid",
				query = "SELECT h FROM Handle h WHERE h.pid = :pid")
})
public class Handle {
	public static enum Type {
		COLLECTION, METADATA;
	}
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false, updatable = false)
	private long id = -1;
	@Column(name = "pid", nullable = false, updatable = false, unique = true)
	private String pid;
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "type", nullable = false, updatable = false)
	private Type type;
	@Column(name = "target", nullable = false, updatable = false)
	private long target;
	
	@SuppressWarnings("unused")
	private Handle() {
	}

	public Handle(String pid, Type type, long target) {
		this.pid = pid;
		this.type = type;
		this.target = target;
	}

	public long getId() {
		return id;
	}

	public String getPid() {
		return pid;
	}

	public Type getType() {
		return type;
	}
	
	public long getTarget() {
		return target;
	}

	public static String createPid() {
		return UUID.randomUUID().toString();
	}

} // class Handle
