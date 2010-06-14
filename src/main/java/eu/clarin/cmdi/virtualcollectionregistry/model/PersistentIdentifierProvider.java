package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Map;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;

public abstract class PersistentIdentifierProvider {
    public static final String BASE_URI = "pid_provider.base_uri";
    public static final String PROVIDER_CLASS = "pid_provider.class";

    protected PersistentIdentifierProvider(Map<String, String> config)
            throws VirtualCollectionRegistryException {
        super();
    }

    public abstract PersistentIdentifier createIdentifier(VirtualCollection vc)
            throws VirtualCollectionRegistryException;

    public abstract void updateIdentifier(String pid, URI target)
            throws VirtualCollectionRegistryException;

    public abstract void deleteIdentifier(String pid)
            throws VirtualCollectionRegistryException;

    protected PersistentIdentifier doCreate(VirtualCollection vc,
            PersistentIdentifier.Type type, String identifier) {
        return new PersistentIdentifier(vc, type, identifier);
    }

    protected static String getConfigParameter(Map<String, String> config,
            String parameter) throws VirtualCollectionRegistryException {
        String value = config.get(parameter);
        if (value == null) {
            throw new VirtualCollectionRegistryException("configuration "
                    + "parameter \"" + parameter + "\" is not set");
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new VirtualCollectionRegistryException("configuration "
                    + "parameter \"" + parameter + "\" is invalid");
        }
        return value;
    }

    public static PersistentIdentifierProvider createProvider(
            Map<String, String> config)
            throws VirtualCollectionRegistryException {
        /*
         * XXX: instantiating the pid provider should probably done in a
         * different and less complicated way.
         */
        String clazzName = getConfigParameter(config, PROVIDER_CLASS);
        try {
            Class<?> clazz = Class.forName(clazzName);
            if (PersistentIdentifierProvider.class.isAssignableFrom(clazz)) {
                Constructor<?> c = clazz.getConstructor(Map.class);
                return (PersistentIdentifierProvider) c.newInstance(config);
            }
        } catch (Exception e) {
            throw new VirtualCollectionRegistryException("error initalizing "
                    + "persistent identifier provider", e);
        }
        throw new VirtualCollectionRegistryException("invalid persistent "
                + "identifier provider (" + clazzName + ")");
    }

} // abstract class PersistentIdentifierProvider
