package de.uni_leipzig.asv.clarin.webservices.pidservices2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * Stores most(!) relevant information of a PID JSON object retrieved from the GWDG
 * 
 * @author Thomas Eckart
 */
public class PidObject {
	private final String handleIdentifier;
	private final Map<HandleField, String> fieldMap;

	public PidObject(String pid, JSONArray pidJsonArray) {
		this.handleIdentifier = pid;

		fieldMap = new HashMap<HandleField, String>();
		String jsonPath;
		for (HandleField fieldName : HandleField.values()) {
			try {
				jsonPath = "$..[?(@.type=='" + fieldName + "')].parsed_data[0]";
				fieldMap.put(fieldName, JsonPath.read(pidJsonArray, jsonPath).toString());
			} catch (PathNotFoundException pnfe) {
				fieldMap.put(fieldName, null);
			}
		}
	}

	/**
	 * Returns handle identifier
	 * 
	 * @return handle identifier
	 */
	public String getHandleIdentifier() {
		return handleIdentifier;
	}

	/**
	 * Returns stored value in EPIC handle for a specific field
	 * 
	 * @param field
	 *            name of the stored field
	 * @return value of the stored field ('parsed_data'), may be null
	 */
	public String getValue(HandleField fieldName) {
		return fieldMap.get(fieldName);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(handleIdentifier);
		sb.append(" (Values: ");
		Iterator<HandleField> fieldIterator = fieldMap.keySet().iterator();
		while (fieldIterator.hasNext()) {
			HandleField field = fieldIterator.next();
			sb.append(" " + field + "=\"" + fieldMap.get(field) + "\"");
		}
		sb.append(")");
		return sb.toString();
	}
}