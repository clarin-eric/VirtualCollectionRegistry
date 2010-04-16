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
		
		public String toXmlString() {
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
		return name.toXmlString();
	}

	public boolean isRequired() {
		return required;
	}

	public boolean validateArgument(String value) {
		switch (name) {
		case IDENTIFIER:
			return identifierRegEx.matcher(value).matches();
		default:
			// XXX: implement validation
			return true;
		}
	}

} // class Argument
