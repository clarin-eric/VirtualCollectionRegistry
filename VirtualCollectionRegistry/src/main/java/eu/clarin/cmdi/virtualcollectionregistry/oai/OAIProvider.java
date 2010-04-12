package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;
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

	public void process(VerbContextImpl ctx) throws OAIException {
		String verbName = ctx.getParameter("verb");
		if (verbName == null) {
			throw new OAIException("missing verb");
		}
		if (ctx.isParameterMultivalued("verb")) {
			throw new OAIException("multiple verb arguments");
		}

		Verb verb = null;
		for (Verb v : verbs) {
			if (verbName.equals(v.getName())) {
				verb = v;
				break;
			}
		} // for

		if (verb != null) {
			Set<String> remaining = ctx.getParameterNames();
			for (Argument arg : verb.getArguments()) {
				String value = ctx.getParameter(arg.getName());
				if ((value == null) && arg.isRequired()) {
					ctx.addError(OAIErrorCode.BAD_ARGUMENT,
									"OAI verb '" + verbName +
	                                "' requires argument '" +
	                                arg.getName() + "'");
				} else {
					remaining.remove(arg.getName());
					if (ctx.isParameterMultivalued(arg.getName())) {
						ctx.addError(OAIErrorCode.BAD_ARGUMENT,
								"OAI verb '" + verbName +
								"' illegally has multiple values for " +
								"argument '" + arg.getName() + "'");
					} else {
						logger.debug("key: {}, value: {}",
									 arg.getName(), value);
						try {
							arg.validateArgument(value);
							ctx.setArgument(arg.getName(), value);
						} catch (OAIException e) { 
							ctx.addError(OAIErrorCode.BAD_ARGUMENT,
									"Value of argument '" +
									arg.getName() + "' of OAI verb '" +
									verbName + "' is invalid: " +
									e.getMessage());
						}
					}
				}
			} // for

			if (!remaining.isEmpty()) {
				for (String key : remaining) {
					ctx.addError(OAIErrorCode.BAD_ARGUMENT,
								 "superfluous argument '" + key + "'");
				}
			} else {
				logger.debug("processing verb '{}'", verb.getName());
				verb.process(ctx);
			}
		} else {
			ctx.addError(OAIErrorCode.BAD_VERB, "illegal OAI verb '" +
					verbName + "'");
		}

		if (ctx.hasErrors()) {
			PrintWriter out = new PrintWriter(ctx.getWriter());
			out.println("OAI protocol error:");
			// XXX: just testing ...
			logger.error("OAI-CONTEXT: {}", ctx.getRequestURI());
			for (VerbContext.Error error : ctx.getErrors()) {
				logger.error("OAI-ERROR ({}): {}",
							 error.getCode(), error.getMessage());
				out.println(error.getCode() + ": " + error.getMessage());
			}
		}
	}

	public static OAIProvider instance() {
		return s_instance;
	}

} // class OAIProvider
