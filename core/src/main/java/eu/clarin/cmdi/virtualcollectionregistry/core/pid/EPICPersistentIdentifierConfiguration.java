package eu.clarin.cmdi.virtualcollectionregistry.core.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.impl.PidWriterImpl;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.interfaces.PidWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
//import org.springframework.context.annotation.Profile;

/**
 * Configuration for
 * {@link EPICPersistentIdentifierProvider EPIC API v2 persistent identifier provider}.
 * Reads a number of configuration values; assumes a
 * {@link PropertyPlaceholderConfigurer} to be configured.
 *
 * The following properties are required:
 * <ul>
 * <li>pid_provider.epic.service_base_url</li>
 * <li>pid_provider.epic.handle_prefix</li>
 * <li>pid_provider.epic.user</li>
 * <li>pid_provider.epic.password</li>
 * </ul>
 *
 * @author twagoo
 * @see Configuration#Configuration(java.lang.String, java.lang.String,
 * java.lang.String, java.lang.String)
 */
@Profile("vcr.pid.epic")
@org.springframework.context.annotation.Configuration
public class EPICPersistentIdentifierConfiguration implements PublicConfiguration {

    @Value("${pid_provider.epic.service_base_url}")
    private String serviceBaseUrl;
    @Value("${pid_provider.epic.handle_prefix}")
    private String handlePrefix;
    @Value("${pid_provider.epic.user}")
    private String user;
    @Value("${pid_provider.epic.password}")
    private String password;

    @Bean
    public Configuration configuration() {
        return new Configuration(serviceBaseUrl, handlePrefix, user, password);
    }

    @Bean
    public PidWriter pidWriter() {
        return new PidWriterImpl();
    }

    @Override
    public String getBaseUrl() {
        return serviceBaseUrl;
    }

    @Override
    public String getPrefix() {
        return handlePrefix;
    }

    @Override
    public String getUsername() {
        return user;
    }
}
