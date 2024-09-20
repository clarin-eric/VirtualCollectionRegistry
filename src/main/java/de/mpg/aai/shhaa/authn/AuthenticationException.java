package de.mpg.aai.shhaa.authn;

import de.mpg.aai.security.auth.AuthException;


/**
 * exception indicates authentication issues, 
 * like failed login etc...
 * @author megger
 *
 */
public class AuthenticationException extends AuthException {
	/** @see java.io.Serializable */
	private static final long serialVersionUID = 3923653203010534550L;
	
	/** 
	 * default constructor
	 */
	public AuthenticationException() {
	}
	/** {@inheritDoc} */
	public AuthenticationException(String message) {
		super(message);
	}
	/** {@inheritDoc} */
	public AuthenticationException(Throwable cause) {
		super(cause);
	}
	/** {@inheritDoc} */
	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
}
