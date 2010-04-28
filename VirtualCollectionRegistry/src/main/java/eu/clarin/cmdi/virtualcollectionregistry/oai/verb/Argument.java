package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.UUID;
import java.util.regex.Pattern;

public class Argument {
	public static final String ARG_FROM            = "from";
	public static final String ARG_IDENTIFIER      = "identifier";
	public static final String ARG_METADATAPREFIX  = "metadataPrefix";
	public static final String ARG_RESUMPTIONTOKEN = "resumptionToken";
	public static final String ARG_SET             = "set";
	public static final String ARG_UNTIL           = "until";
	private static final Pattern dateRegEx =
		Pattern.compile("\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}Z)?");
	private static final Pattern identifierRegEx =
		Pattern.compile("oai:[a-z][a-z\\d\\-]*(\\.[a-z][a-z\\d\\-]*)+:" +
	                    "[\\w\\.!~\\*'\\(\\);/\\?:&=\\+\\$,%]+",
	                    Pattern.CASE_INSENSITIVE);
	private static final Pattern metadataPrefixRegEx =
		Pattern.compile("[\\w\\.!~\\*'\\(\\)]+", Pattern.CASE_INSENSITIVE);
	private static final Pattern setSpecRegEx =
		Pattern.compile("[\\w\\.!~\\*'\\(\\)]+(:[\\w\\.!~\\*'\\(\\)]+)*",
					    Pattern.CASE_INSENSITIVE);
	
	private final String name;
	private final boolean required;

	public Argument(String name, boolean required) {
		this.name     = name;
		this.required = required;
	}

	public String getName() {
		return name;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean checkArgument(String value) {
		if (name.equals(ARG_FROM) || name.equals(ARG_UNTIL)) {
			return dateRegEx.matcher(value).matches();
		} else if (name.equals(ARG_IDENTIFIER)) {
			return identifierRegEx.matcher(value).matches();
		} else if (name.equals(ARG_METADATAPREFIX)) {
			return metadataPrefixRegEx.matcher(value).matches();
		} else if (name.equals(ARG_SET)) {
			return setSpecRegEx.matcher(value).matches();
		} else if (name.equals(ARG_RESUMPTIONTOKEN)) {
			try {
				UUID.fromString(value);
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		} else {
			throw new InternalError("invalid name: " + name);
		}
	}

} // class Argument
