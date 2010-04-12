package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument.Name;

public class GetRecordVerb extends Verb {
	private static final List<Argument> s_arguments =
		Arrays.asList(new Argument(Name.IDENTIFIER, true),
					  new Argument(Name.METADATAPREFIX, true));

	@Override
	public String getName() {
		return "GetRecord";
	}

	@Override
	public List<Argument> getArguments() {
		return s_arguments;
	}

	@Override
	public void process(VerbContext ctx) {
		logger.debug("process GET-RECORD");
	}

} // class GetRecordVerb
