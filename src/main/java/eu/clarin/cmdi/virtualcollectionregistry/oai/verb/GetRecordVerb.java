package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument.Name;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument.Use;

public class GetRecordVerb extends Verb {
	private static final List<Argument> s_arguments =
		Arrays.asList(new Argument(Name.IDENTIFIER, Use.REQUIRED),
					  new Argument(Name.METADATAPREFIX, Use.REQUIRED));

	@Override
	public String getName() {
		return "GetRecord";
	}

	@Override
	protected List<Argument> getArguments() {
		return s_arguments;
	}

	@Override
	protected void doProcess(VerbContext ctx) {
		logger.debug("process GET-RECORD");
	}

} // class GetRecordVerb
