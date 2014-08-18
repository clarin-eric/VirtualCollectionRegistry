package de.uni_leipzig.asv.clarin.webservices.pidservices2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores some information needed for establishing connection to resolver server
 * 
 * @author Thomas Eckart
 * @author Twan Goosen
 * 
 */
public class Configuration {
	private final static Logger LOG = LoggerFactory.getLogger(Configuration.class);

	private String serviceBaseURL;
	private String handlePrefix;
	private String user;
	private String password;

	/**
	 * Creates a configuration from the <em>config.properties</em> file. Expecting the following properties in the file:
	 * <ul>
	 * <li>SERVICE_BASE_URL</li>
	 * <li>HANDLE_PREFIX</li>
	 * <li>USER</li>
	 * <li>PASSWORD</li>
	 * </ul>
	 * 
	 * A missing property will result in a runtime exception
	 * 
	 * @throws IOException
	 *             if the properties file could not be read
	 */
	public Configuration() throws IOException {
		this(readProperties("config.properties"));
	}

	public static Properties readProperties(String file) throws IOException {
		final Properties properties = new Properties();
		try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
			properties.load(stream);
		}
		return properties;
	}

	/**
	 * Creates a configuration from properties. Expecting the following properties:
	 * <ul>
	 * <li>SERVICE_BASE_URL</li>
	 * <li>HANDLE_PREFIX</li>
	 * <li>USER</li>
	 * <li>PASSWORD</li>
	 * </ul>
	 * A missing property will result in a runtime exception
	 * 
	 * 
	 * @param properties
	 */
	public Configuration(Properties properties) {
		this(getRequiredProperty(properties, "SERVICE_BASE_URL"), getRequiredProperty(properties, "HANDLE_PREFIX"),
				getRequiredProperty(properties, "USER"), getRequiredProperty(properties, "PASSWORD"));
	}

	private static String getRequiredProperty(Properties properties, String name) {
		final String value = properties.getProperty(name);
		if (value == null) {
			throw new RuntimeException("Required property " + name + " missing!");
		}
		LOG.debug("Read PID client configuration parameter {}: '{}'", name, value);
		return value;
	}

	/**
	 * 
	 * @param serviceBaseURL
	 * @param handlePrefix
	 * @param user
	 * @param password
	 */
	public Configuration(final String serviceBaseURL, final String handlePrefix, final String user,
			final String password) {
		this.serviceBaseURL = serviceBaseURL;
		this.handlePrefix = handlePrefix;
		this.user = user;
		this.password = password;
	}

	public void setServiceBaseURL(final String serviceBaseURL) {
		this.serviceBaseURL = serviceBaseURL;
	}

	public void setHandlePrefix(final String handlePrefix) {
		this.handlePrefix = handlePrefix;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * @return serviceBaseURL (e.g. http://handle.gwdg.de:8080/pidservice/)
	 */
	public String getServiceBaseURL() {
		return serviceBaseURL;
	}

	/**
	 * @return handle prefix (e.g. 11022)
	 */
	public String getHandlePrefix() {
		return handlePrefix;
	}

	/**
	 * @return resolver account name
	 */
	public String getUser() {
		return user;
	}

	/**
	 * 
	 * @return resolver password
	 */
	public String getPassword() {
		return password;
	}
}
