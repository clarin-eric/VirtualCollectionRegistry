package eu.clarin.cmdi.virtualcollectionregistry;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.impl.PidWriterImpl;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.pid.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class PidProviderServiceImpl implements PidProviderService {
    private final static Logger logger = LoggerFactory.getLogger(PidProviderServiceImpl.class);

    @Value("${eu.clarin.cmdi.virtualcollectionregistry.pidprovider.configfile:pidproviders.properties}")
    private String configFile;

    @Value("${eu.clarin.cmdi.virtualcollectionregistry.pidprovider.basedir:.}")
    private String baseDir;

    //@Value("${eu.clarin.cmdi.virtualcollectionregistry.base_uri}")
    //private String baseUri;

    public final static String DEFAULT_INFIX = "VC-";

    @Value("${pid_provider.epic.infix:VC-}")
    private String infix;

    private List<PersistentIdentifierProvider> providers = new LinkedList<>();

    private final static String LATEST_SUFFIX = "-latest";

    public PidProviderServiceImpl() {}

    @PostConstruct
    protected final void init() throws VirtualCollectionRegistryException {
        logger.info("Loading PID Provider configuration from {}/{}", baseDir, configFile);
        Path cfg = Paths.get(baseDir, configFile);
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(cfg.toFile()));

            //Fetch provider indices from properties file
            List<Integer> indices = new LinkedList<>();
            for(String key: props.stringPropertyNames()) {
                if(key.startsWith("pid.provider.")) {
                    String remainder = key.substring("pid.provider.".length());
                    int idx = remainder.indexOf(".");
                    int provider_index = Integer.parseInt(remainder.substring(0, idx));
                    if(!indices.contains(provider_index)) {
                        indices.add(provider_index);
                    }
                } else {
                    logger.warn("Skipping unrecognized key: {}", key);
                }
            }

            //Process provider for each index
            for(int i : indices) {
                String type = props.getProperty("pid.provider."+i+".type");
                switch(type) {
                    case "DUMMY": loadDummyProvider(props, "pid.provider."+i); break;
                    case "GWDG": loadGwdgProvider(props, "pid.provider."+i); break;
                    case "EPIC": loadEpicProvider(props, "pid.provider."+i); break;
                    case "DOI": loadDoiProvider(props, "pid.provider."+i); break;
                    default:
                        throw new VirtualCollectionRegistryException("Pid provider type = "+type+" is not supported (index="+i+")");
                }
            }

        } catch(FileNotFoundException ex) {
            throw new VirtualCollectionRegistryException("PID Provider configuration file ("+cfg.toAbsolutePath().toString()+") not found (dir="+baseDir+").", ex);
        } catch(IOException ex) {
            throw new VirtualCollectionRegistryException("Failed to read PID Provider configuration file. ", ex);
        }

        //Validation
        int primary_count = 0;
        for(PersistentIdentifierProvider provider : providers) {
            if(provider.isPrimaryProvider()) {
                primary_count++;
            }
            logger.debug("Loaded {} PID provider (primary={})", provider.getId(), provider.isPrimaryProvider());
        }

        if(primary_count != 1) {
            throw new VirtualCollectionRegistryException("We need exactly 1 primary provider, instead we found "+primary_count);
        }
    }

    private void loadDummyProvider(Properties props, String key_prefix) throws VirtualCollectionRegistryException {
        String primary = props.getProperty(key_prefix+".primary");
        DummyPersistentIdentifierProvider provider = new DummyPersistentIdentifierProvider();
        provider.setPrimaryProvider(Boolean.valueOf(primary));
        providers.add(provider);
    }

    private void loadEpicProvider(Properties props, String key_prefix) {
        String baseUrl = props.getProperty(key_prefix+".baseurl");
        String primary = props.getProperty(key_prefix+".primary");
        String prefix = props.getProperty(key_prefix+".prefix");
        String infix = props.getProperty(key_prefix+".infix");
        String username = props.getProperty(key_prefix+".username");
        String password = props.getProperty(key_prefix+".password");
        Configuration config = new Configuration(baseUrl, prefix, username, password);
        EPICPersistentIdentifierProvider provider =
            new EPICPersistentIdentifierProvider(new PidWriterImpl(), config);
        //provider.setBaseUri(baseUri);
        provider.setInfix(infix);
        provider.setPrimaryProvider(Boolean.valueOf(primary));
        providers.add(provider);
    }

    private void loadGwdgProvider(Properties props, String key_prefix) throws VirtualCollectionRegistryException {
        String baseUrl = props.getProperty(key_prefix+".baseurl");
        String primary = props.getProperty(key_prefix+".primary");
        String username = props.getProperty(key_prefix+".username");
        String password = props.getProperty(key_prefix+".password");
        Map<String, String> config = new HashMap<>();
        config.put(GWDGPersistentIdentifierProvider.BASE_URI, baseUrl);
        config.put(GWDGPersistentIdentifierProvider.USERNAME, username);
        config.put(GWDGPersistentIdentifierProvider.PASSWORD, password);
        GWDGPersistentIdentifierProvider provider =
                new GWDGPersistentIdentifierProvider(config);
        provider.setPrimaryProvider(Boolean.valueOf(primary));
        providers.add(provider);
    }

    private void loadDoiProvider(Properties props, String key_prefix) {
        String baseUrl = props.getProperty(key_prefix+".baseurl");
        String primary = props.getProperty(key_prefix+".primary");
        String prefix = props.getProperty(key_prefix+".prefix");
        String infix = props.getProperty(key_prefix+".infix");
        logger.info("Loaded DOI infix: "+infix);
        String username = props.getProperty(key_prefix+".username");
        String password = props.getProperty(key_prefix+".password");
        Configuration config = new Configuration(baseUrl, prefix, username, password);
        DoiPersistentIdentifierProvider provider = new DoiPersistentIdentifierProvider(new DoiPidWriter(), config);
        provider.setPrimaryProvider(Boolean.valueOf(primary));
        provider.setInfix(infix);
        providers.add(provider);
    }

    @Override
    public List<PersistentIdentifierProvider> getProviders() {
        return providers;
    }

    /**
     * Use each provider to create a pid and return the pid created by the primary provider.
     *
     * @param vc
     * @return
     * @throws VirtualCollectionRegistryException
     */
    public List<PersistentIdentifier> createIdentifiers(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException {
        List<PersistentIdentifier> pids = new LinkedList<>();
        for(PersistentIdentifierProvider provider : providers) {
            pids.add(provider.createIdentifier(vc, permaLinkService));

        }
        return pids;
    }

    /**
     * Use each provider to create a latest pid and return the pid created by the primary provider.
     *
     * @param vc
     * @return
     * @throws VirtualCollectionRegistryException
     */
    public List<PersistentIdentifier> createLatestIdentifiers(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException {
        List<PersistentIdentifier> pids = new LinkedList<>();
        for(PersistentIdentifierProvider provider : providers) {
            PersistentIdentifier pid = provider.createIdentifier(vc, LATEST_SUFFIX, permaLinkService);
            pid.setPrimary(false);
            pid.setLatest(true);
            pids.add(pid);

        }
        return pids;
    }

    /**
     * Update all identifiers for the collection specified by id with the specified newUrl
     * @param pid
     * @param newUrl
     */
    public void updateLatestIdentifierUrl(PersistentIdentifier pid, String newUrl) throws VirtualCollectionRegistryException {
        for(PersistentIdentifierProvider provider : providers) {
            pid.getPidType()
        }
    }

    public void updateIdentifierUrl(PersistentIdentifier pid, String newUrl) throws VirtualCollectionRegistryException {

    }
}
