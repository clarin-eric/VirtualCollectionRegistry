package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.regex.Pattern;

public class Argument {
	public static enum Name {
		FROM,            // from
		IDENTIFIER,      // identifier
		METADATAPREFIX,  // metadataPrefix
		RESUMPTIONTOKEN, // resumptionToken
		SET,             // set
		UNTIL;           // until
		
		public String getAsString() {
			switch (this) {
			case FROM:
				return "from";
			case IDENTIFIER:
				return "identifier";
			case METADATAPREFIX:
				return "metadataPrefix";
			case RESUMPTIONTOKEN:
				return "resumptionToken";
			case SET:
				return "set";
			case UNTIL:
				return "until";
			default:
				throw new InternalError("invalid name: " + this);
			}
		}
	} // enum Name
	private static final Pattern identifierRegEx =
		Pattern.compile("oai:[a-z][a-z\\d\\-]*(\\.[a-z][a-z\\d\\-]*)+:" +
	                    "[\\w\\.!~\\*'\\(\\);/\\?:&=\\+\\$,%]+",
	                    Pattern.CASE_INSENSITIVE);
	private static final Pattern metadataPrefixRegEx =
		Pattern.compile("[\\w\\.!~\\*'\\(\\)]+", Pattern.CASE_INSENSITIVE);
	private static final Pattern dateRegEx =
		Pattern.compile("\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}Z)?");
	
	private final Name name;
	private final boolean required;

	public Argument(Name name, boolean required) {
		this.name     = name;
		this.required = required;
	}

	public Name getName() {
		return name;
	}

	public String getNameAsString() {
		return name.getAsString();
	}

	public boolean isRequired() {
		return required;
	}

	public boolean checkArgument(String value) {
		switch (name) {
		case FROM:
			/* FALL_THROUGH */
		case UNTIL:
			return dateRegEx.matcher(value).matches();
		case IDENTIFIER:
			return identifierRegEx.matcher(value).matches();
		case METADATAPREFIX:
			return metadataPrefixRegEx.matcher(value).matches();
		case SET:
			return true;
		case RESUMPTIONTOKEN:
			return true;
		default:
			throw new InternalError("invalid argument name");
		} // switch
	}

} // class Argument
