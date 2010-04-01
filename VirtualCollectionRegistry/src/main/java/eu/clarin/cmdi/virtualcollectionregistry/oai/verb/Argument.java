package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;


public class Argument {
	public static enum Name {
		FROM,            // from
		IDENTIFIER,      // identifier
		METADATAPREFIX,  // metadataPrefix
		RESUMPTIONTOKEN, // resumptionToken
		SET,             // set
		UNTIL;           // until
	} // enum Name

	public static enum Use {
		REQUIRED, OPTIONAL;
	} // enum Use

	private final Name name;
	private final Use use;

	public Argument(Name name, Use use) {
		this.name = name;
		this.use = use;
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
		return (use == Use.REQUIRED);
	}

	public void validateArgument(String value) throws OAIException {
		// XXX: implement validation
	}

} // class Argument
