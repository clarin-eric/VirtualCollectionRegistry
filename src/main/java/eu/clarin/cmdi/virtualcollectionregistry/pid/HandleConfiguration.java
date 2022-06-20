package eu.clarin.cmdi.virtualcollectionregistry.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;

import java.io.IOException;
import java.util.Properties;

public class HandleConfiguration extends Configuration {

    private final static String DEFAULT_GLOBAL_RESOLVER = "";

    private String resolverUrl;

    public HandleConfiguration() throws IOException {
        super();
    }

    public HandleConfiguration(Properties properties) {
        super(properties);
    }

    public HandleConfiguration(final String serviceBaseURL, final String handlePrefix, final String user, final String password) {
        this(serviceBaseURL, handlePrefix, user, password, DEFAULT_GLOBAL_RESOLVER);

    }

    public HandleConfiguration(final String serviceBaseURL, final String handlePrefix, final String user, final String password, final String resolverUrl) {
        super(serviceBaseURL, handlePrefix, user, password);
        this.resolverUrl = resolverUrl;
    }
    public String getResolverUrl() {
        return resolverUrl;
    }
}
