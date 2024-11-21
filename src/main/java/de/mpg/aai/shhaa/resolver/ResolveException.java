package de.mpg.aai.shhaa.resolver;

import de.mpg.aai.security.auth.AuthException;

/**
 * exception indicates issues in attribute composition/resolving  
 * @author megger
 *
 */
public class ResolveException extends AuthException {
	/** @see java.io.Serializable */
	private static final long serialVersionUID = -4901132953871033790L;
	
	
	/**
	 * default constructor
	 */
	public ResolveException() {
	}
	/** {@inheritDoc} */
	public ResolveException(String message) {
		super(message);
	}
	/** {@inheritDoc} */
	public ResolveException(Throwable cause) {
		super(cause);
	}
	/** {@inheritDoc} */
	public ResolveException(String message, Throwable cause) {
		super(message, cause);
	}
}
