package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResumptionToken {
	private final String id;
	private long expirationDate;
	private int cursor = -1;
	private int completeListSize = -1;
	
	private final Map<String, Object> properties = new HashMap<String, Object>();
	
	ResumptionToken() {
		this.id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}
	
	public void setExpirationDate(long expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Date getExpirationDate() {
		return new Date(expirationDate);
	}

	public boolean checkExpired(long timestamp) {
		return (timestamp > expirationDate);
	}

	public void setCursor(int cursor) {
		this.cursor = cursor;
	}
	
	public int getCursor() {
		return cursor;
	}

	public void setCompleteListSize(int completeListSize) {
		this.completeListSize = completeListSize;
	}
	
	public int getCompleteListSize() {
		return completeListSize;
	}

	public void setProperty(String key, Object value) {
		if (value != null) {
			properties.put(key, value);
		}
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

} // class ResumptionToken
