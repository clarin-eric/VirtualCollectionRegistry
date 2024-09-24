package de.mpg.aai.shhaa.authz;

import de.mpg.aai.shhaa.model.AuthAttributes;

/**
 * interface defining abstract authorization rule 
 * @author megger
 *
 */
public interface Rule {
	/**
	 * determines whether the given credentials are sufficient to grant access according to this rule 
	 * @param credentials the credentials to check by
	 * @return true if given credentials meet conditions/requirements of this rule (whatever implementation), false otherwise
	 */
	public boolean evaluate(AuthAttributes credentials);
	/**
	 * adds a child rule (element) to this (parent) rule
	 * @param rule to add (as child rule to this)
	 */
	public void addRule(Rule rule);
}
