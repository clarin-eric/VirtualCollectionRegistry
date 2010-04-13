package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

public class Argument {
	public static enum Name {
		FROM,            // from
		IDENTIFIER,      // identifier
		METADATAPREFIX,  // metadataPrefix
		RESUMPTIONTOKEN, // resumptionToken
		SET,             // set
		UNTIL;           // until
	} // enum Name
	private final Name name;
	private final boolean required;

	public Argument(Name name, boolean required) {
		this.name     = name;
		this.required = required;
	}

	public String getName() {
		switch (name) {
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
			throw new InternalError("invalid name: " + name);
		}
	}

	public boolean isRequired() {
		return required;
	}

	public boolean validateArgument(String value) {
		// XXX: implement validation
		return true;
	}

} // class Argument
