package de.uni_leipzig.asv.clarin.webservices.pidservices2.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.HandleField;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.PidObject;

/**
 * Requests information about handle from handle server
 * 
 * @author Thomas Eckart
 */
public interface PidResolver {

	/**
	 * Get information about handle from handle server.
	 * 
	 * @param configuration
	 * @param pid
	 *            Handle Id
	 * @return all information from handle server in JSON
	 * @throws IOException
	 */
	public JSONArray resolvePidAsJSON(final Configuration configuration, final String pid) throws IOException;

	/**
	 * Get information about handle from handle server.
	 * 
	 * The returned object only provides some fields of the handle (like referenced URL). To have access to the complete content use resolvePidAsJSON().
	 * 
	 * @param configuration
	 * @param pid
	 *            Handle Id
	 * @return all information from handle server as simple Java object
	 * @throws IOException
	 */
	public PidObject resolvePidAsPOJO(final Configuration configuration, final String pid) throws IOException;

	/**
	 * Search all handles with matching field assignments.
	 * 
	 * @param configuration
	 * @param fieldMap
	 *            searched handle field assignments
	 * @return Map handles -> handle fields as JSONArray
	 * @throws IOException
	 */
	public Map<String, JSONArray> searchPidAsJSON(final Configuration configuration, Map<HandleField, String> fieldMap)
			throws IOException;

	/**
	 * Search all handles with matching field assignments.
	 * 
	 * @param configuration
	 * @param fieldMap
	 *            searched handle field assignments
	 * @return Map handles -> handle fields as PidObject
	 * @throws IOException
	 */
	public Map<String, PidObject> searchPidAsPOJO(final Configuration configuration, Map<HandleField, String> fieldMap)
			throws IOException;

	/**
	 * Search all handles with matching field assignments.
	 * 
	 * @param configuration
	 * @param fieldMap
	 *            searched handle field assignments
	 * @return List of handle identifiers
	 * @throws IOException
	 */
	public List<String> searchPidAsList(final Configuration configuration, Map<HandleField, String> fieldMap)
			throws IOException;
}
