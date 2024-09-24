package de.mpg.aai.shhaa.context;

import java.security.Principal;
import java.util.Date;
import java.util.Set;

import javax.security.auth.Subject;

import de.mpg.aai.security.auth.model.AbstractReadOnly;
import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.AuthAttributes;
import de.mpg.aai.shhaa.model.AuthPrincipal;


public class AuthenticationContext extends AbstractReadOnly {
	/** the AAI session-ID: an aai session might differ from a (pure single webapp) session-id (like JSESSIONID) */
	private String		sessionID;
	/** name/id/label of the identity-provider the principal has logged-in/authenticated */
	private String		identiyProviderID;
	/** the login time the current principal has been logged-in/authenticated */
	private Date		authnTime;
	/** the current user representation */
	private Subject		subject;
	/** flag to indicate whether this context represents the fallback "user" */
	private boolean		fallback;
	
	
	/**
	 * default constructor
	 */
	public AuthenticationContext() {
	}
	
	
	/**
	 * sets this instance to read-only and its subject
	 * => allows no more modifications (via setter) 
	 * @see #getSubject()
	 * @see Subject#setReadOnly()
	 */
	@Override
	public void setReadOnly() {
		super.setReadOnly();
		if(this.subject != null) {
			this.getAuthPrincipal().setReadOnly();
			this.getAuthAttributes().setReadOnly();
			this.subject.setReadOnly();
		}
	}
	
	
	/**
	 * @return the sessionID
	 */
	public String getSessionID() {
		return this.sessionID;
	}
	/**
	 * @param authSessionID the session-ID to set
	 */
	public void setSessionID(String authSessionID) {
		this.checkReadOnly();
		this.sessionID = authSessionID;
	}
	
	
	/**
	 * @return the identiyProviderID
	 */
	public String getIdentiyProviderID() {
		return this.identiyProviderID;
	}
	/**
	 * @param identiyProviderID the identiyProviderID to set
	 */
	public void setIdentiyProviderID(String idpID) {
		this.checkReadOnly();
		this.identiyProviderID = idpID;
	}
	
	
	/**
	 * @return the authnTime
	 */
	public Date getLoginTime() {
		return this.authnTime;
	}
	/**
	 * @param authnTime the authnTime to set
	 */
	public void setLoginTime(Date loginTime) {
		this.checkReadOnly();
		this.authnTime = loginTime;
	}
	
	
	/**
	 * @return the subject
	 */
	public Subject getSubject() {
		return this.subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Subject user) {
		this.checkReadOnly();
		this.subject = user;
	}
	
	/**
	 * @return the AAI principal from the Subject
	 */
	public AuthPrincipal getAuthPrincipal() {
		if(this.subject == null)
			throw new IllegalStateException("subject in authenticationContext is null");
		Set<Principal> pcpls = this.subject.getPrincipals();
		if(pcpls == null || pcpls.isEmpty())
			throw new IllegalStateException("Subject holds no principals");
		// we expect that this iterator always provides the very same - first - element from the set
		// <=> although Set is not specified as sorted, the actual impl in Subject IS using a LinkedList
		// if the target is not the first item, one can suspect something odd
		for(Principal pcpl : pcpls) {
			if(pcpl instanceof AuthPrincipal) {
				return (AuthPrincipal) pcpl;
			}
		}
		throw new IllegalStateException("Subject holds no AuthPrincipal");
	}
	
	/**
	 * @return the AAI Attributes (UNBACKED reference!) from the Subject's (public) credentials
	 */
	@SuppressWarnings("unchecked")
	public AuthAttributes getAuthAttributes() {
		AuthAttributes result = this.getAuthPrincipal().getAttribues();	// aaiPcpl never null (IllegalStateE before)
		if(result != null)
			return result;
		if(this.subject == null)
			throw new NullPointerException("subject in authenticationContext is null");
		Set<?> unbackedRef = this.subject.getPublicCredentials(AuthAttribute.class);
		result = new AuthAttributes((Set<AuthAttribute<?>>) unbackedRef);
		return result;
	}
	
	/**
	 * @return whether this current context represents a fallback session/user (<=> no valid shib-user/session)
	 */
	public boolean isFallback() {
		return this.fallback;
	}
	/**
	 * @param val sets {@link #fallback}
	 */
	public void setFallback(boolean val) {
		this.checkReadOnly();
		this.fallback = val;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer("session ");
		result.append(this.sessionID);
		result.append(" at ").append(this.identiyProviderID);
		result.append(" on ").append(this.authnTime);
//		result.append(" by ").append(this.subject != null ? this.subject.toString() : "no-subject");
		return result.toString();
	}
}
