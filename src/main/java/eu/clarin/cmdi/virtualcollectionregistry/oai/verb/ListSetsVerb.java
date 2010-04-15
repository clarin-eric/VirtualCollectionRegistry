package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument.Name;

public class ListSetsVerb extends Verb {
	private static final List<Argument> s_arguments =
		Arrays.asList(new Argument(Name.RESUMPTIONTOKEN, false));

	@Override
	public String getName() {
		return "ListSets";
	}

	@Override
	public List<Argument> getArguments() {
		return s_arguments;
	}

	@Override
	public void process(VerbContext ctx) throws OAIException {
		logger.debug("process LIST-SETS");

		OAIRepositoryAdapter repository = ctx.getRepository();
		if (repository.isUsingSets()) {
			throw new OAIException("Repository supportes set, but set "+
					"support is not available, yet!");
		} else {
			ctx.addError(OAIErrorCode.NO_SET_HIERARCHY,
					     "This repository does not support sets");
		}
	}

} // class ListSetsVerb
