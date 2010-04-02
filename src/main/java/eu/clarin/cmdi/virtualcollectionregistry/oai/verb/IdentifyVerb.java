package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Collections;
import java.util.List;

public class IdentifyVerb extends Verb {

	@Override
	public String getName() {
		return "Identify";
	}

	@Override
	protected List<Argument> getArguments() {
		return Collections.emptyList();
	}

	@Override
	protected void doProcess(VerbContext ctx) {
		logger.debug("process IDENTIFY");
	}


} // class IdentifyVerb
