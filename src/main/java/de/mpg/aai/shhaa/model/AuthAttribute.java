package de.mpg.aai.shhaa.model;

import java.util.Collection;
import java.util.Set;

/**
 * interface representing (authorization) attribute
 * @author megger
 *
 * @param <T> type of the attribute value(s)
 */
public interface AuthAttribute<T> {
	
	
	/**
	 * @return the attribute ID, its Name
	 */
	public abstract String getID();
	
	
	/**
	 * adds the given value to this attribute
	 * @param val value to add
	 * @return as {@link Collection#add(Object)}
	 */
	public abstract boolean addValue(T val);
	/**
	 * adds the given values to this attribute
	 * @param vals collection of values to add
	 * @return as {@link Collection#addAll(Collection)}
	 */
	public abstract boolean addValues(Collection<T> vals);
	
	/**
	 * @return number of values held by this attribute
	 */
	public abstract int size();
	/**
	 * checks whether this attribute actually contains the given value
	 * @param val value to be checked if present in this attribute
	 * @return as {@link Collection#contains(Object)}
	 */
	public abstract boolean contains(T val);
	/**
	 * @return provides (the FIRST of all) this attributes value(s)
	 */
	public abstract T getValue();
	/**
	 * @return set of all this attributes values
	 */
	public abstract Set<T> getValues();
	
	/**
	 * removes the given value from this attribute
	 * @param val value to be removed
	 * @return as {@link Collection#remove(Object)}
	 */
	public abstract boolean removeValue(T val);
	/**
	 * clears (removes) all this attribute's values
	 */
	public abstract void clear();
	
	/**
	 * sets this attribute to read-only
	 */
	public void setReadOnly();
	/**
	 * @return whether this attribute is in read-only status 
	 * - are modifications (setter) allowed or not
	 */
	public boolean isReadOnly();

}