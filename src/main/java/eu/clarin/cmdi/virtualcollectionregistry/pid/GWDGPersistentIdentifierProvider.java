package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.ServletUtils;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("vcr.pid.gwdg")
public class GWDGPersistentIdentifierProvider implements
        PersistentIdentifierProvider, Serializable {

    public static final String BASE_URI = "eu.clarin.cmdi.virtualcollectionregistry.base_uri";

    private final String id = "GWDG";

    private boolean primary = false;

    @Override
    public String getId() {
        return id;
    }

    private static enum Attribute {

        PID, URL, CREATOR, EXPDATE;

        public static Attribute fromString(String s) {
            if (s.equalsIgnoreCase("pid")) {
                return PID;
            } else if (s.equalsIgnoreCase("url")) {
                return URL;
            } else if (s.equalsIgnoreCase("creator")) {
                return CREATOR;
            } else if (s.equalsIgnoreCase("expdate")) {
                return EXPDATE;
            }
            return null;
        }

        @Override
        public String toString() {
            switch (this) {
                case PID:
                    return "pid";
                case URL:
                    return "url";
                case CREATOR:
                    return "creator";
                case EXPDATE:
                    return "expdate";
                default:
                    throw new InternalError();
            }
        }
    } // private enum Attribute

    public static final String USERNAME = "pid_provider.username";
    public static final String PASSWORD = "pid_provider.password";
    private static final String SERVICE_URI_BASE
            = "http://handle.gwdg.de:8080/pidservice/";
    private static final String USER_AGENT
            = "CLARIN-VirtualCollectionRegisty/1.0";
    private static final Logger logger
            = LoggerFactory.getLogger(GWDGPersistentIdentifierProvider.class);
    private String base_uri = null;
    private String username = null;
    private String password = null;
    private XMLInputFactory factory;

    @Autowired
    public GWDGPersistentIdentifierProvider(ServletContext servletContext) throws VirtualCollectionRegistryException {
        this(ServletUtils.createParameterMap(servletContext));
    }

    public GWDGPersistentIdentifierProvider(Map<String, String> config)
            throws VirtualCollectionRegistryException {
        super();
        try {
            String base_uri = getConfigParameter(config, BASE_URI);
            if (!base_uri.endsWith("/")) {
                base_uri = base_uri + "/";
            }
            URI uri = new URI(base_uri);
            this.base_uri = uri.toString();
        } catch (URISyntaxException e) {
            throw new VirtualCollectionRegistryException("configuration "
                    + "parameter \"" + BASE_URI + "\" is invalid", e);
        }
        this.username = getConfigParameter(config, USERNAME);
        this.password = getConfigParameter(config, PASSWORD);

        this.factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                Boolean.TRUE);
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc)
            throws VirtualCollectionRegistryException {
        if (vc == null) {
            throw new NullPointerException("vc == null");
        }
        logger.debug("creating handle for virtual collection \"{}\"",
                vc.getId());
        try {
            String target = makeCollectionURI(vc);
            URI serviceURI = URI.create(SERVICE_URI_BASE + "write/create");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("url", target));
            Map<Attribute, String> props = invokeWebService(serviceURI, params);
            String pid = props.get(Attribute.PID);
            if (pid == null) {
                throw new VirtualCollectionRegistryException(
                        "no handle returned");
            }
            logger.info("created handle \"{}\" for virtual collection \"{}\"",
                    pid, vc.getId());
            return new PersistentIdentifier(vc, PersistentIdentifier.Type.HANDLE, primary, pid);
        } catch (VirtualCollectionRegistryException e) {
            throw new RuntimeException("failed to create handle", e);
        }
    }

    @Override
    public void updateIdentifier(String pid, URI target)
            throws VirtualCollectionRegistryException {
        if (pid == null) {
            throw new NullPointerException("pid == null");
        }
        if (pid.isEmpty()) {
            throw new IllegalArgumentException("pid is empty");
        }
        if (target == null) {
            throw new NullPointerException("target == null");
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pid", pid));
        params.add(new BasicNameValuePair("url", target.toString()));
        URI serviceURI = URI.create(SERVICE_URI_BASE + "write/modify");
        invokeWebService(serviceURI, params);
        logger.info("updated handle \"{}\"", pid);
    }

    @Override
    public void deleteIdentifier(String pid)
            throws VirtualCollectionRegistryException {
        if (pid == null) {
            throw new NullPointerException("pid == null");
        }
        if (pid.isEmpty()) {
            throw new IllegalArgumentException("pid is empty");
        }
        /*
         * actually one cannot delete a handle, but we can set an expired date
         * to mark it invalid
         */
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pid", pid));
        params.add(new BasicNameValuePair("expdate", "1970-01-01"));
        URI serviceURI = URI.create(SERVICE_URI_BASE + "write/modify");
        invokeWebService(serviceURI, params);
        logger.info("deleted/expired handle \"{}\"", pid);
    }

    private String makeCollectionURI(VirtualCollection vc) {
        return base_uri + "service/clarin-virtualcollection/" + vc.getId();
    }

    private Map<Attribute, String> invokeWebService(URI serviceTargetURI,
            List<NameValuePair> formparams)
            throws VirtualCollectionRegistryException {
        // force xml encoding
        formparams.add(new BasicNameValuePair("encoding", "xml"));

        DefaultHttpClient client = null;
        try {
            client = new DefaultHttpClient();
            int port = serviceTargetURI.getPort() != -1 ? serviceTargetURI
                    .getPort() : AuthScope.ANY_PORT;
            client.getCredentialsProvider().setCredentials(
                    new AuthScope(serviceTargetURI.getHost(), port),
                    new UsernamePasswordCredentials(username, password));
            // disable expect continue, GWDG does not like very well
            client.getParams().setParameter(
                    CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
            // set a proper user agent
            client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                    USER_AGENT);
            HttpPost request = new HttpPost(serviceTargetURI);
            request.addHeader("Accept", "text/xml, application/xml");
            request.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
            HttpContext ctx = new BasicHttpContext();

            logger.debug("invoking GWDG service at {}", serviceTargetURI);
            HttpResponse response = client.execute(request, ctx);
            StatusLine status = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            Map<Attribute, String> props = Collections.emptyMap();

            logger.debug("GWDG Service status: {}", status.toString());
            if ((status.getStatusCode() >= 200)
                    && (status.getStatusCode() <= 299) && (entity != null)) {
                String encoding = EntityUtils.getContentCharSet(entity);
                if (encoding == null) {
                    encoding = "UTF-8";
                }

                XMLStreamReader reader = factory.createXMLStreamReader(entity
                        .getContent(), encoding);
                props = new HashMap<Attribute, String>();
                while (reader.hasNext()) {
                    reader.next();

                    int type = reader.getEventType();
                    if (type != XMLStreamConstants.START_ELEMENT) {
                        continue;
                    }
                    Attribute attribute = Attribute.fromString(reader
                            .getLocalName());
                    if (attribute != null) {
                        if (!reader.hasNext()) {
                            throw new VirtualCollectionRegistryException(
                                    "unexpected end of data stream");
                        }
                        reader.next();
                        if (reader.getEventType() != XMLStreamConstants.CHARACTERS) {
                            throw new VirtualCollectionRegistryException(
                                    "unexpected element type: "
                                    + reader.getEventType());
                        }
                        String value = reader.getText();
                        if (value == null) {
                            throw new VirtualCollectionRegistryException(
                                    "element \"" + attribute + "\" was empty");
                        }
                        value = value.trim();
                        if (!value.isEmpty()) {
                            props.put(attribute, value);
                        }
                    }
                }

            } else {
                logger.debug("GWDG Handle service failed: {}", status);
                request.abort();
                throw new VirtualCollectionRegistryException(
                        "error invoking GWDG handle service");
            }
            return props;
        } catch (VirtualCollectionRegistryException e) {
            throw e;
        } catch (Exception e) {
            logger.debug("GWDG Handle service failed", e);
            throw new VirtualCollectionRegistryException(
                    "error invoking GWDG handle service", e);
        } finally {
            if (client != null) {
                client.getConnectionManager().shutdown();
            }
        }
    }

    private static String getConfigParameter(Map<String, String> config,
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

    @Override
    public boolean ownsIdentifier(String pid) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isPrimaryProvider() {
        return this.primary;
    }

    @Override
    public void setPrimaryProvider(boolean primary) {
        this.primary = primary;
    }

    @Override
    public String getInfix() {
        return null;
    }

    @Override
    public PublicConfiguration getPublicConfiguration() {
        return null;
    }

} // class GWDGPersistentIdentifierProvider
