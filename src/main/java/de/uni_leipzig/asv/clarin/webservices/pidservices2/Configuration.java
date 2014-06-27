package de.uni_leipzig.asv.clarin.webservices.pidservices2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Stores some information needed for establishing connection to resolver server
 *
 * @author Thomas Eckart
 *
 */
public class Configuration {

    private final static Logger LOG = Logger.getLogger(Configuration.class);

    private final String serviceBaseURL;
    private final String handlePrefix;
    private final String user;
    private final String password;

    public Configuration() throws IOException {
        this(readProperties());
    }

    /**
     * private constructor for Singleton
     *
     * @return
     * @throws java.io.IOException
     */
    public static Properties readProperties() throws IOException {
        final Properties properties = new Properties();
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream("config.properties"))) {
            properties.load(stream);
        }
        return properties;
    }

    public Configuration(Properties properties) {
        serviceBaseURL = properties.getProperty("SERVICE_BASE_URL");
        handlePrefix = properties.getProperty("HANDLE_PREFIX");
        user = properties.getProperty("USER");
        password = properties.getProperty("PASSWORD");
    }

    /**
     * constructor
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
