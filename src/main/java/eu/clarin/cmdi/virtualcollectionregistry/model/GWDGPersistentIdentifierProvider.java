package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;

public class GWDGPersistentIdentifierProvider extends
		PersistentIdentifierProvider {
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
	private static final String SERVICE_URI_BASE =
		"http://handle.gwdg.de:8080/pidservice/";
	private static final String USER_AGENT =
		"CLARIN-VirtualCollectionRegisty/1.0";
	private static final Logger logger =
		LoggerFactory.getLogger(GWDGPersistentIdentifierProvider.class);
	private String base_uri = null;
	private String username = null;
	private String password = null;
	private XMLInputFactory factory;

	/* XXX: refactor Internal and GWDG PID class/providers, so only one
	 *        PID class exists.
	 *        Maybe: store type in generic PID class
	 *        inject dependency to PID provider in PID classes and
	 *        make factory method in provider for creating URIs
	 */
	public GWDGPersistentIdentifierProvider(Map<String,String> config)
			throws VirtualCollectionRegistryException {
		super(config);
		try {
			String base_uri = getParameter(config, BASE_URI);
			if (!base_uri.endsWith("/")) {
				base_uri = base_uri + "/";
			}
			URI uri = new URI(base_uri);
			this.base_uri = uri.toString();
		} catch (URISyntaxException e) {
			throw new VirtualCollectionRegistryException("configuration " +
				      "parameter \"" + BASE_URI + "\" is invalid", e);
		}
		this.username = getParameter(config, USERNAME);
		this.password = getParameter(config, PASSWORD);
		
		this.factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
							Boolean.TRUE);
	}

	public PersistentIdentifier createPersistentIdentifier(VirtualCollection vc)
			throws VirtualCollectionRegistryException {
		logger.debug("creating handle for virtual collection \"{}\"",
				vc.getUUID());
		try {
			String target  = makeCollectionURI(vc);
			// XXX: testing
			// URI serviceURI = URI.create(SERVICE_URI_BASE + "write/create");
			URI serviceURI = URI.create(SERVICE_URI_BASE + "write/modify");

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("url", target));
			// XXX: testing
			params.add(new BasicNameValuePair("pid", "11858/00-232Z-0000-0000-40AE-F"));
			Map<Attribute, String> props = invokeWebService(serviceURI, params);
			String pid = props.get(Attribute.PID);
			if (pid == null) {
				throw new VirtualCollectionRegistryException(
					"no handle returned");
			}
			logger.info("created handle \"{}\" for virtual collection \"{}\"",
					pid, vc.getUUID());
			return new GWDGPersistentIdentifier(vc, pid);
		} catch (VirtualCollectionRegistryException e) {
			throw new RuntimeException("failed to create handle", e);
		}
	}

	@SuppressWarnings("unused")
	private void update(String pid, URI target) throws VirtualCollectionRegistryException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pid", pid));
		params.add(new BasicNameValuePair("url", target.toString()));
		URI serviceURI = URI.create(SERVICE_URI_BASE + "write/modify");
		invokeWebService(serviceURI, params);
	}

	private String makeCollectionURI(VirtualCollection vc) {
		return base_uri + "service/clarin-virtualcollection/" + vc.getUUID();
	}

	private static String getParameter(Map<String, String> config,
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

	private Map<Attribute, String> invokeWebService(URI serviceTargetURI,
			List<NameValuePair> formparams)
			throws VirtualCollectionRegistryException {
		// force xml encoding
		formparams.add(new BasicNameValuePair("encoding", "xml"));

		DefaultHttpClient client = null;
		try {
			client = new DefaultHttpClient();
			int port = serviceTargetURI.getPort() != -1
			         ? serviceTargetURI.getPort()
                     : AuthScope.ANY_PORT
                     ;
			client.getCredentialsProvider().setCredentials(
					new AuthScope(serviceTargetURI.getHost(), port),
					new UsernamePasswordCredentials(username, password)
			);
			// disable expect continue, GWDG does not like very well
			client.getParams()
				.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, Boolean.FALSE);
			// set a proper user agent
			client.getParams()
				.setParameter(HttpProtocolParams.USER_AGENT, USER_AGENT);
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
			if ((status.getStatusCode() >= 200) &&
				(status.getStatusCode() <= 299) &&
				(entity != null)) {
				String encoding = EntityUtils.getContentCharSet(entity);
				if (encoding == null) {
					encoding = "UTF-8";
				}

				XMLStreamReader reader = factory
						.createXMLStreamReader(entity.getContent(), encoding);
				props = new HashMap<Attribute, String>(); 
				while (reader.hasNext()) {
					reader.next();
					
					int type = reader.getEventType();
					if (type != XMLStreamConstants.START_ELEMENT) {
						continue;
					}
					Attribute attribute =
						Attribute.fromString(reader.getLocalName());
					if (attribute != null) {
						if (!reader.hasNext()) {
							throw new VirtualCollectionRegistryException(
								"unexpected end of data stream");
						}
						reader.next();
						if (reader.getEventType() !=
								XMLStreamConstants.CHARACTERS) {
							throw new VirtualCollectionRegistryException(
								"unexpected element type: " +
								reader.getEventType());
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

} // class GWDGPersistentIdentifierProvider
