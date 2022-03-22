package eu.clarin.cmdi.virtualcollectionregistry.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.HandleField;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.interfaces.PidWriter;
import eu.clarin.cmdi.virtualcollectionregistry.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.PidProviderServiceImpl;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import java.io.Serializable;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * PID provider that uses the EPIC API v2 to communicate with a handle provider.
 * Wraps around the PID Resolver / PID Writer library by Thomas Eckart (Leipzig
 * University)
 *
 * @author twagoo
 * @see ​http://www.pidconsortium.eu/
 * @see http://epic.gwdg.de/wiki/index.php/EPIC:API
 * @see ​https://github.com/CatchPlus/EPIC-API-v2/
 */
@Service
@Profile("vcr.pid.epic")
public class EPICPersistentIdentifierProvider implements PersistentIdentifierProvider, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(EPICPersistentIdentifierProvider.class);
    private final transient PidWriter pidWriter;
    private final transient Configuration configuration;

    private final String id = "EPIC";

    private boolean primary = false;

    private String infix;

    /**
     *
     * @param pidWriter PID writer implementation to use
     * @param configuration configuration to be passed to PID writer methods
     */
    @Autowired
    public EPICPersistentIdentifierProvider(PidWriter pidWriter, Configuration configuration) {
        this.pidWriter = pidWriter;
        this.configuration = configuration;
    }

    @Override
    public String getId() {
        return id;
    }

    private String generatePidValue(Long id, String suffix) {
        return String.format("%s%d%s", getInfix(), id, suffix);
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException {
        return createIdentifier(vc, "", permaLinkService);
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc, String suffix, PermaLinkService permaLinkService)
            throws VirtualCollectionRegistryException {
        logger.debug("creating handle for virtual collection \"{}\"", vc.getId());
        final Map<HandleField, String> fieldMap = createPIDFieldMap(vc, permaLinkService);
        try {
            final String requestedPid = generatePidValue(vc.getId(), suffix);
            final String pid = pidWriter.registerNewPID(configuration, fieldMap, requestedPid);
            return new PersistentIdentifier(vc, PersistentIdentifier.Type.HANDLE, primary, pid);
        } catch (HttpException ex) {
            throw new VirtualCollectionRegistryException("Could not create EPIC identifier", ex);
        }
    }

    private Map<HandleField, String> createPIDFieldMap(VirtualCollection vc, PermaLinkService permaLinkService) {
        final Map<HandleField, String> pidMap = new EnumMap<>(HandleField.class);
        final String url = permaLinkService.getCollectionUrl(vc);
        pidMap.put(HandleField.URL, url);
        pidMap.put(HandleField.TITLE, vc.getName());
        if (!vc.getCreators().isEmpty()) {
            pidMap.put(HandleField.CREATOR, vc.getCreators().get(0).getPerson());
        }
        return pidMap;
    }

    @Override
    public void updateIdentifier(PersistentIdentifier pid, URI target) throws VirtualCollectionRegistryException {

        throw new UnsupportedOperationException("Not supported yet.");



    }

    @Override
    public void deleteIdentifier(String pid) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    //Make sure we return the default infix value if an empty infix has been set
    @Override
    public String getInfix() {
        if(this.infix == null || this.infix.isEmpty()) {
            return PidProviderServiceImpl.DEFAULT_INFIX;
        }
        return infix;
    }

    @Override
    public boolean ownsIdentifier(String pid) {
        return pid.toLowerCase().startsWith(configuration.getHandlePrefix().toLowerCase());
    }

    @Override
    public boolean isPrimaryProvider() {
        return primary;
    }

    @Override
    public void setPrimaryProvider(boolean primary) {
        this.primary = primary;
    }

    @Override
    public PublicConfiguration getPublicConfiguration() {
        return new PublicConfiguration() {
            @Override
            public String getBaseUrl() {
                return configuration != null ? configuration.getServiceBaseURL() : "";
            }
            @Override
            public String getPrefix() {
                return configuration != null ? configuration.getHandlePrefix() : "";
            }
            @Override
            public String getUsername() {
                return configuration != null ? configuration.getUser() : "";
            }
        };
    }
}
