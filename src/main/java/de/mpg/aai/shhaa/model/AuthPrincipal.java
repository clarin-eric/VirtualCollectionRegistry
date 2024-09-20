package de.mpg.aai.shhaa.model;

import de.mpg.aai.security.auth.model.BasePrincipal;


/**
 * simple implementation of java.security.Principal,
 * plus an additional property for its {@link AuthAttributes} 
 * 
 * @author megger
 *
 */
public class AuthPrincipal extends BasePrincipal {
	/** this users {@link AuthAttribute}s */
	private AuthAttributes	attribues;
	/**
	 * constructor, initializes username
	 * @param username uid to set
	 */
	public AuthPrincipal(String username) {
		super(username);
	}
	/**
	 * @param attbs the attributes to set
	 */
	public void setAttribues(AuthAttributes attbs) {
		this.attribues = attbs;
	}
	/**
	 * @return the attributes
	 */
	public AuthAttributes getAttribues() {
		return this.attribues;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setReadOnly() {
		super.setReadOnly();
		if(this.attribues != null)
			this.attribues.setReadOnly();
	}
}
