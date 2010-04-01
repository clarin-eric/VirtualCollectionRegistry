package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.GetRecordVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.IdentifyVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Verb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.VerbContext;

public class OAIProvider {
	private static final Logger logger =
		LoggerFactory.getLogger(OAIProvider.class);
	private static final OAIProvider s_instance = new OAIProvider();
	private final List<Verb> verbs;
	
	private OAIProvider() {
		super();
		verbs = new ArrayList<Verb>();
		verbs.add(new IdentifyVerb());
		verbs.add(new GetRecordVerb());
	}

	public void process(VerbContext ctx) throws OAIException {
		Verb verb = null;

		String verbArg = ctx.getArgument("verb");
		if (verbArg != null && !verbArg.isEmpty()) {
			for (Verb v : verbs) {
				if (verbArg.equals(v.getName())) {
					verb = v;
					break;
				}
			} // for
			if (verb == null) {
				ctx.addError(OAIErrorCode.BAD_VERB, "illegal OAI verb '" +
						verbArg + "'");
			}
		} else {
			ctx.addError(OAIErrorCode.BAD_VERB, "missing OAI verb");
		}

		if ((verb != null) && !ctx.hasErrors()) {
			logger.debug("processing verb '{}'", verb.getName());
			verb.process(ctx);
		}
		if (ctx.hasErrors()) {
			// XXX: just testing ...
			for (VerbContext.Error error : ctx.getErrors()) {
				logger.error("OAI-ERROR ({}): {}",
							 error.getCode(), error.getMessage());
			}
		}
	}

	public static OAIProvider instance() {
		return s_instance;
	}

} // class OAIProvider
