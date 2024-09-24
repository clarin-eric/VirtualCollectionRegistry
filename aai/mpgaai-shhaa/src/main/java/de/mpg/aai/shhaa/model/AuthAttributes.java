package de.mpg.aai.shhaa.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.mpg.aai.security.auth.model.AbstractReadOnly;


/**
 * simple container holding {@link AuthAttribute}s
 * @author megger
 *
 */
public class AuthAttributes extends AbstractReadOnly {
	/** map of the attributes */
	private Map<String, AuthAttribute<?>>	elements;
	
	/**
	 * constructor, initializes the data
	 * @param data attributes to be added to this container
	 */
	public AuthAttributes(Set<AuthAttribute<?>> data) {
		this.elements = new LinkedHashMap<String, AuthAttribute<?>>();
		for(AuthAttribute<?> attb : data) {
			this.elements.put(attb.getID(), attb);
		}
	}
	
	/**
	 * provides the attribute with the given ID
	 * @param id attribute id/name to look-up/provide
	 * @return found attribute, <code>null<code> if found none
	 */
	public AuthAttribute<?> get(String id) {
		return this.elements.get(id);
	}
	/**
	 * @return the number of attributes held in this instance 
	 */
	public int size() {
		return this.elements.size();
	}
	
	
	/**
	 * @return a Set of IDs of the attributes held by this instance
	 */
	public Set<String> getIDs() {
		return this.elements != null
			? this.elements.keySet()
			: null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setReadOnly() {
		super.setReadOnly();
		if(this.elements == null)
			return;
		for(AuthAttribute<?> attb : this.elements.values()) {
			attb.setReadOnly();
		}
	}
}
