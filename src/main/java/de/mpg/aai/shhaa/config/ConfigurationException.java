package de.mpg.aai.shhaa.config;

import de.mpg.aai.security.auth.AuthException;


/**
 * exception indicates configuration issues
 * @author	megger
 */
public class ConfigurationException extends AuthException {
	/** @see java.io.Serializable */
	private static final long serialVersionUID = 1784332359304557409L;
	
	
	/**
	 * default constructor
	 */
	public ConfigurationException() {
	}
	/** {@inheritDoc} */
	public ConfigurationException(String message) {
		super(message);
	}
	/** {@inheritDoc} */
	public ConfigurationException(Throwable cause) {
		super(cause);
	}
	/** {@inheritDoc} */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
