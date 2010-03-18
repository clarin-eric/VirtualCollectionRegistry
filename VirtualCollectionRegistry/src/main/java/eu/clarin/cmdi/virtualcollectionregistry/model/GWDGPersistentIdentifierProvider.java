package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;

public class GWDGPersistentIdentifierProvider extends
		PersistentIdentifierProvider {
	public static final String USERNAME = "pid_provider.username";
	public static final String PASSWORD = "pid_provider.password";
	private static final URI SERVICE_URI =
		URI.create("http://handle.gwdg.de:8080/pidservice/");
	private static final Logger logger =
		LoggerFactory.getLogger(GWDGPersistentIdentifierProvider.class);
	private String base_uri = null;
	private String username = null;
	private String password = null;

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
	}

	public PersistentIdentifier createPersistentIdentifier(VirtualCollection vc)
			throws VirtualCollectionRegistryException {
		logger.debug("creating handle for virtual collection {}", vc.getUUID());
		URI target    = makeCollectionURI(vc);
		String handle = createHandle(target);
		logger.info("created handle {} for virtual collection {}",
					handle, vc.getUUID());
		return new GWDGPersistentIdentifier(vc, handle);
	}

	private URI makeCollectionURI(VirtualCollection vc) {
		return URI.create(base_uri +
                          "service/clarin-virtualcollection/" + vc.getUUID());
	}

	private String createHandle(URI target) {
		logger.debug("pid target: {}", target);
		foo(target);
		return "12345/007";
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

	private void foo(URI target) {
		try {
			URI serviceURI = URI.create(SERVICE_URI.toString() + "write/modify");
			DefaultHttpClient client = new DefaultHttpClient();
			int port = serviceURI.getPort() != -1
			         ? serviceURI.getPort()
                     : AuthScope.ANY_PORT
                     ;
			client.getCredentialsProvider().setCredentials(
					new AuthScope(serviceURI.getHost(), port),
					new UsernamePasswordCredentials(username, password)
			);
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("encoding", "xml"));
			formparams.add(new BasicNameValuePair("url", target.toString()));
			formparams.add(new BasicNameValuePair("pid", "11858/00-232Z-0000-0000-40AE-F"));
			
			HttpPost request = new HttpPost(serviceURI);
			request.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			HttpContext ctx = new BasicHttpContext();
			
			HttpResponse response = client.execute(request, ctx);
			StatusLine status = response.getStatusLine();
			HttpEntity entity = response.getEntity();
			
			logger.debug("GWDG Service status: {}", status.toString());
			if ((status.getStatusCode() >= 200) &&
				(status.getStatusCode() <= 299) &&
				(entity != null)) {
				String encoding = EntityUtils.getContentCharSet(entity);
				if (encoding == null) {
					encoding = "UTF-8";
				}

				logger.debug("type={}, length={}",
						entity.getContentType(), entity.getContentLength());
				logger.debug("encoding={}", encoding);

				XMLInputFactory factory = XMLInputFactory.newInstance();
				factory.setProperty(XMLInputFactory.IS_COALESCING, true);
				factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
				XMLStreamReader reader = factory.createXMLStreamReader(response.getEntity().getContent(), encoding);
				while (reader.hasNext()) {
					reader.next();
					if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
						if (reader.getLocalName().equalsIgnoreCase("pid")) {
							reader.next();
							logger.trace("-> PID: " + reader.getText());
						} else if (reader.getLocalName().equalsIgnoreCase("expdate")) {
							reader.next();
							logger.trace("-> EXP-Date: " + reader.getText());
						} else if (reader.getLocalName().equalsIgnoreCase("url")) {
							reader.next();
							logger.trace("-> URI: " + reader.getText());
						}
					}
				}
			} else {
				logger.error("http failed: {}", status);
			}
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			logger.error("http failed", e);
		}
	}

} // class GWDGPersistentIdentifierProvider
