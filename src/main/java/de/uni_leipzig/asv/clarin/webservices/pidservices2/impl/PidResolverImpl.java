package de.uni_leipzig.asv.clarin.webservices.pidservices2.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import net.sf.json.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.HandleField;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.PidObject;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.interfaces.PidResolver;

/**
 * Requests information about handle from handle server
 * 
 * @author Thomas Eckart
 */
public class PidResolverImpl implements PidResolver {
	private final static Logger LOG = LoggerFactory.getLogger(PidResolverImpl.class);

	@Override
	public JSONArray resolvePidAsJSON(final Configuration configuration, final String pid) throws IOException {
		LOG.debug("Searching for \"" + pid + "\" at " + configuration.getServiceBaseURL());

		final Client client = Client.create();
		client.addFilter(new HTTPBasicAuthFilter(configuration.getUser(), configuration.getPassword()));
		final WebResource webResource = client.resource(configuration.getServiceBaseURL() + pid);

		// query
		final ClientResponse clientResponse = webResource.accept("application/json;charset=UTF-8").get(
				ClientResponse.class);
		if (clientResponse.getStatus() != 200) {
			throw new IOException("Received a different response than expected (200): " + clientResponse.getStatus()
					+ " (URL: '" + webResource.toString() + "')");
		}

		return JSONArray.fromObject(clientResponse.getEntity(String.class));
	}

	@Override
	public PidObject resolvePidAsPOJO(final Configuration configuration, final String pid) throws IOException {
		return new PidObject(pid, resolvePidAsJSON(configuration, pid));
	}

	@Override
	public Map<String, JSONArray> searchPidAsJSON(final Configuration configuration, Map<HandleField, String> fieldMap)
			throws IOException {
		Map<String, JSONArray> jsonArrayMap = new HashMap<String, JSONArray>();

		for (String handle : searchPidAsList(configuration, fieldMap)) {
			jsonArrayMap.put(handle, resolvePidAsJSON(configuration, handle));
		}

		return jsonArrayMap;
	}

	@Override
	public Map<String, PidObject> searchPidAsPOJO(final Configuration configuration, Map<HandleField, String> fieldMap)
			throws IOException {
		Map<String, JSONArray> jsonArrayMap = searchPidAsJSON(configuration, fieldMap);
		Map<String, PidObject> pidObjectsMap = new HashMap<String, PidObject>();
		Iterator<String> handleIterator = jsonArrayMap.keySet().iterator();
		while (handleIterator.hasNext()) {
			String handle = handleIterator.next();
			pidObjectsMap.put(handle, new PidObject(handle, jsonArrayMap.get(handle)));
		}

		return pidObjectsMap;
	}

	@Override
	public List<String> searchPidAsList(final Configuration configuration, Map<HandleField, String> fieldMap)
			throws IOException {
		LOG.debug("Searching at " + configuration.getServiceBaseURL() + " with: " + fieldMap);
		List<String> handleList = new ArrayList<String>();

		final Client client = Client.create();
		client.addFilter(new HTTPBasicAuthFilter(configuration.getUser(), configuration.getPassword()));
		final WebResource webResource = client.resource(configuration.getServiceBaseURL()
				+ configuration.getHandlePrefix());

		// add URL parameters
		final MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		Iterator<HandleField> fieldTypeIterator = fieldMap.keySet().iterator();
		while (fieldTypeIterator.hasNext()) {
			HandleField tmpFieldType = fieldTypeIterator.next();
			queryParams.add(tmpFieldType.toString(), fieldMap.get(tmpFieldType));
		}

		// query
		final ClientResponse response = webResource.queryParams(queryParams).accept("application/json;charset=UTF-8")
				.get(ClientResponse.class);
		if (response.getStatus() != 200) {
			throw new IOException("Received a different response than expected (200): " + response.getStatus()
					+ " (URL: '" + webResource.toString() + "')");
		}

		// parse response and get all handle fields
		JSONArray handleIdJSONArray = JSONArray.fromObject(response.getEntity(String.class));
		for (int i = 0; i < handleIdJSONArray.size(); i++) {
			String handle = configuration.getHandlePrefix() + "/" + handleIdJSONArray.getString(i);
			handleList.add(handle);
			LOG.debug("Found handle " + i + "\t" + handle);
		}

		return handleList;
	}
}
