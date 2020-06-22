package eu.clarin.cmdi.virtualcollectionregistry.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.HandleField;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.interfaces.PidWriter;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class EPICPersistentIdentifierProvider implements PersistentIdentifierProvider {

    private static final Logger logger = LoggerFactory.getLogger(EPICPersistentIdentifierProvider.class);
    private final PidWriter pidWriter;
    private final Configuration configuration;

    @Value("${eu.clarin.cmdi.virtualcollectionregistry.base_uri}")
    private String baseUri;

    @Value("${pid_provider.epic.infix:VCR-}")
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
    public PersistentIdentifier createIdentifier(VirtualCollection vc) throws VirtualCollectionRegistryException {
        logger.debug("creating handle for virtual collection \"{}\"", vc.getId());
        final Map<HandleField, String> fieldMap = createPIDFieldMap(vc);
        try {
            final String requestedPid = String.format("%s%d", infix, vc.getId());
            final String pid = pidWriter.registerNewPID(configuration, fieldMap, requestedPid);
            return new PersistentIdentifier(vc, PersistentIdentifier.Type.HANDLE, pid);
        } catch (HttpException ex) {
            throw new VirtualCollectionRegistryException("Could not create EPIC identifier", ex);
        }
    }

    private Map<HandleField, String> createPIDFieldMap(VirtualCollection vc) {
        final Map<HandleField, String> pidMap = new EnumMap<>(HandleField.class);
        pidMap.put(HandleField.URL, makeCollectionURI(vc));
        pidMap.put(HandleField.TITLE, vc.getName());
        if (!vc.getCreators().isEmpty()) {
            pidMap.put(HandleField.CREATOR, vc.getCreators().get(0).getPerson());
        }
        return pidMap;
    }

    @Override
    public void updateIdentifier(String pid, URI target) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteIdentifier(String pid) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String makeCollectionURI(VirtualCollection vc) {
        String base = baseUri;
        if(base.endsWith("/")) {
            base = base.substring(0, base.length()-1);
        }
        return String.format("%s/service/virtualcollections/%d", base, vc.getId());
    }

    protected void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
    
    protected void setInfix(String infix) {
        this.infix = infix;
    }

}
