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
 *
 * @author twagoo
 */
@Service
@Profile("vcr.pid.epic2")
public class EPIC2PersistentIdentifierProvider implements PersistentIdentifierProvider {

    private static final Logger logger = LoggerFactory.getLogger(EPIC2PersistentIdentifierProvider.class);
    private final PidWriter pidWriter;
    private final Configuration configuration;

    @Value("${pid_provider.base_uri}")
    private String baseUri;

    @Autowired
    public EPIC2PersistentIdentifierProvider(PidWriter pidWriter, Configuration configuration) {
        this.pidWriter = pidWriter;
        this.configuration = configuration;
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc) throws VirtualCollectionRegistryException {
        logger.debug("creating handle for virtual collection \"{}\"", vc.getId());
        final Map<HandleField, String> fieldMap = createPIDFieldMap(vc);
        try {
            final String pid = pidWriter.registerNewPID(configuration, fieldMap);
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
        return baseUri + "service/clarin-virtualcollection/" + vc.getId();
    }

    protected void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

}
