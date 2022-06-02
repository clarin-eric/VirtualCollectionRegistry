package eu.clarin.cmdi.virtualcollectionregistry.pid;

/**
 * Public configuration without any secrets
 */
public interface PublicConfiguration {
    String getBaseUrl();
    String getPrefix();
    String getUsername();
}
