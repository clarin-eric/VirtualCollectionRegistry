package de.mpg.aai.shhaa.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.mpg.aai.security.auth.model.AbstractReadOnly;


/**
 * basic generic implementation of {@link AuthAttribute}
 * @author megger
 *
 * @param <T> type of the attribute value(s)
 */
public  class BaseAuthAttribute<T> extends AbstractReadOnly implements AuthAttribute<T> {
	private String		id;
	private Set<T> 		data 	= new LinkedHashSet<T>();	// using linkedHashSet to keep order as added 
	
	
	public BaseAuthAttribute(String attributeID) {
		this.id = attributeID;
	}
	public BaseAuthAttribute(String attributeID, T value) {
		this(attributeID);
		this.addValue(value);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public String getID() {
		return this.id;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean addValue(T val) {
		this.checkReadOnly();
		return this.data.add(val);
	}
	/** {@inheritDoc} */
	@Override
	public boolean addValues(Collection<T> vals) {
		this.checkReadOnly();
		return this.data.addAll(vals);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public int size() {
		return this.data.size();
	}
	/** {@inheritDoc} */
	@Override
	public boolean contains(T val) {
		return this.data.contains(val);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public T getValue()  {
		return this.data.isEmpty()
			? null
			: this.data.iterator().next();
	}
	/** {@inheritDoc} */
	@Override
	public Set<T> getValues() {
		return this.isReadOnly()
			? Collections.unmodifiableSet(this.data)
			: this.data;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean removeValue(T val) {
		this.checkReadOnly();
		return this.data.remove(val);
	}
	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.checkReadOnly();
		this.data.clear();
	}
}
