package de.mpg.aai.shhaa.authz;

import de.mpg.aai.security.auth.AuthException;


/**
 * exception indicates authorization issues, 
 * like access-denied...
 * @author megger
 *
 */
public class AuthorizationException extends AuthException {
	/** @see java.io.Serializable */
	private static final long serialVersionUID = -1906734240266100739L;
	
	/**
	 * default constructor
	 */
	public AuthorizationException() {
	}
	/** {@inheritDoc} */
	public AuthorizationException(String message) {
		super(message);
	}
	/** {@inheritDoc} */
	public AuthorizationException(Throwable cause) {
		super(cause);
	}
	/** {@inheritDoc} */
	public AuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}
}
