package de.mpg.aai.shhaa.config;

/**
 * simple one-method-interface to indicate implementing classes as configurable
 * @author megger
 */
public interface Configurable {
	/**
	 * sets the configuration
	 * @param config current configuration
	 */
	public void setConfig(Configuration config);
}
