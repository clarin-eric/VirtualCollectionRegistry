package de.uni_leipzig.asv.clarin.webservices.pidservices2.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.HandleField;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.interfaces.PidWriter;

/**
 * Registering new handles at handle server or modifying existing PID entries
 * 
 * @author Thomas Eckart
 */
public class PidWriterImpl implements PidWriter {
	private final static Logger LOG = LoggerFactory.getLogger(PidWriterImpl.class);
	private static final Pattern PID_OUTPUT_PATTERN = Pattern.compile(".*location</dt><dd><a href=\"([0-9A-Z-]+)\">.*");

	@Override
	public String registerNewPID(final Configuration configuration, Map<HandleField, String> fieldMap)
			throws HttpException {
		LOG.debug("Try to create handle at " + configuration.getServiceBaseURL() + " with values: " + fieldMap);

		final Client client = Client.create();
		client.addFilter(new HTTPBasicAuthFilter(configuration.getUser(), configuration.getPassword()));
		final WebResource webResource = client.resource(configuration.getServiceBaseURL()
				+ configuration.getHandlePrefix());

		JSONArray jsonArray = createJSONArray(fieldMap);

		final ClientResponse response = webResource.accept("application/json").type("application/json")
				.post(ClientResponse.class, jsonArray.toString());
		if (response.getStatus() != 201) {
			throw new HttpException("" + response.getStatus());
		}

		// TODO CHANGE this ASAP, when GWDG respects accept header
		String responseString = response.getEntity(String.class).trim().replaceAll("\n", "");
		Matcher matcher = PID_OUTPUT_PATTERN.matcher(responseString);
		if (matcher.matches())
			return configuration.getHandlePrefix() + "/" + matcher.group(1);
		else {
			throw new RuntimeException("Unparsable response from " + configuration.getServiceBaseURL());
		}
	}

	@Override
	public void modifyPid(final Configuration configuration, final String pid, Map<HandleField, String> fieldMap) {
		LOG.debug("Try to modify handle \"" + pid + "\" at " + configuration.getServiceBaseURL() + " with new values: "
				+ fieldMap);

		final Client client = Client.create();
		client.addFilter(new HTTPBasicAuthFilter(configuration.getUser(), configuration.getPassword()));
		final WebResource webResource = client.resource(configuration.getServiceBaseURL() + pid);

		JSONArray jsonArray = createJSONArray(fieldMap);
		webResource.accept("application/json").type("application/json").put(ClientResponse.class, jsonArray.toString());
	}

	/**
	 * Generates JSON array that is understood by the EPIC handle service
	 * 
	 * @param fieldMap
	 *            mapping handle field -> value
	 * @return JSON array
	 */
	private JSONArray createJSONArray(Map<HandleField, String> fieldMap) {
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject;

		Iterator<HandleField> fieldIter = fieldMap.keySet().iterator();
		while (fieldIter.hasNext()) {
			jsonObject = new JSONObject();
			HandleField handleFieldTyp = fieldIter.next();
			jsonObject.put("type", handleFieldTyp);
			jsonObject.put("parsed_data", fieldMap.get(handleFieldTyp));
			jsonArray.add(jsonObject);
		}

		return jsonArray;
	}
}
