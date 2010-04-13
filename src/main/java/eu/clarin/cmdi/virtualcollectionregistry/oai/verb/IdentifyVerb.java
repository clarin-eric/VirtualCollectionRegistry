package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Collections;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;

public class IdentifyVerb extends Verb {

	@Override
	public String getName() {
		return "Identify";
	}

	@Override
	public List<Argument> getArguments() {
		return Collections.emptyList();
	}

	@Override
	public void process(VerbContext ctx) throws OAIException {
		logger.debug("process IDENTIFY");
	}


} // class IdentifyVerb
