package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.impl.VerbContextImpl;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.GetRecordVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.IdentifyVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.ListMetadataFormatsVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.ListRecordsVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.ListSetsVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Verb;

public class OAIProvider {
	private static final Logger logger =
		LoggerFactory.getLogger(OAIProvider.class);
	private static final OAIProvider s_instance = new OAIProvider();
	private final List<Verb> verbs;
	private OAIRepositoryAdapter repository;
	
	private OAIProvider() {
		super();
		verbs = new ArrayList<Verb>();
		verbs.add(new IdentifyVerb());
		verbs.add(new ListMetadataFormatsVerb());
		verbs.add(new ListSetsVerb());
		verbs.add(new ListRecordsVerb());
		verbs.add(new GetRecordVerb());
	}

	public void setRepository(OAIRepository repository) throws OAIException {
		if (repository == null) {
			throw new NullPointerException("repository == null");
		}
		if (this.repository != null) {
			throw new IllegalStateException("repository is already set");
		}
		logger.debug("setting repository '{}'", repository.getId());
		this.repository = new OAIRepositoryAdapter(this, repository);
	}

	public boolean hasRepository() {
		return repository != null;
	}

	public void process(VerbContextImpl ctx) throws OAIException {
		if (repository == null) {
			throw new OAIException("no repository configured");
		}
		ctx.setRepository(repository);

		// process verb argument
		Verb verb = null;
		String verbName = ctx.getParameter("verb");
		if (verbName != null) {
			logger.debug("looking up verb '{}'", verbName);
			if (!ctx.isRepeatedParameter("verb")) {
				for (Verb v : verbs) {
					if (verbName.equals(v.getName())) {
						verb = v;
						break;
					}
				} // for
				if (verb == null) {
					ctx.addError(OAIErrorCode.BAD_VERB, "illegal OAI verb '" +
							verbName + "'");
				}
			} else {
				ctx.addError(OAIErrorCode.BAD_VERB, "OAI verb is repeated");
			}
		} else {
			ctx.addError(OAIErrorCode.BAD_VERB, "OAI verb is missing");
		}

		if (verb != null) {
			logger.debug("processing arguments for verb '{}'", verbName);
			ctx.setVerb(verbName);

			// process arguments
			Set<String> remaining = ctx.getParameterNames();
			// FIXME: special handling for resumptionToken
			for (Argument arg : verb.getArguments()) {
				String value = ctx.getParameter(arg.getName().toString());
				if (value != null) {
					remaining.remove(arg.getName().toString());
					if (ctx.isRepeatedParameter(arg.getName().toString())) {
						ctx.addError(OAIErrorCode.BAD_ARGUMENT,
									 "OAI verb '" + verbName +
									 "' has repeated values for argument '" +
									 arg.getName().toString() + "'");
					} else {
						if (!ctx.setArgument(arg, value)) {
							ctx.addError(OAIErrorCode.BAD_ARGUMENT,
										 "Value of argument '" +
										 arg.getName().toString() +
										 "' of OAI verb '" + verbName +
										 "' is invalid (value='" +
										 value + "')");
						}
					}
				} else {
					if (arg.isRequired()) {
						ctx.addError(OAIErrorCode.BAD_ARGUMENT,
									 "OAI verb '" + verbName +
		                             "' is missing required argument '" +
		                             arg.getName().toString() + "'");
					}
				}
			}  // for

			if (!remaining.isEmpty()) {
				logger.debug("received request with illegal arguments");
				for (String key : remaining) {
					ctx.addError(OAIErrorCode.BAD_ARGUMENT,
								 "OAI verb '" + verbName + "' was submitted " +
								 "with illegal argument '" + key + "' "+
								 "(value='" + ctx.getParameter(key) + "')");
				}
			}

			/*
			 * Execute verb, if no error occurred have been recorded.
			 */
			if (!ctx.hasErrors()) {
				if (logger.isDebugEnabled()) {
					logger.debug("processing verb '{}'", verb.getName());
					Map<Argument.Name, String> args = ctx.getUnparsedArguments();
					if (!args.isEmpty()) {
						int i = 0;
						for (Argument.Name name : args.keySet()) {
							logger.debug("argument[" + i++ + "]: {}='{}'",
									name.toString(), args.get(name));
						}
					}
				}
				verb.process(ctx);
			}
		}
		
		/*
		 * If any errors occurred create a proper response.
		 * NOTE: errors may occur, when executing verb, so this block
		 *       cannot be moved
		 */
		if (ctx.hasErrors()) {
			OAIOutputStream out = ctx.getOutputStream();
			for (VerbContext.Error error : ctx.getErrors()) {
				out.writeStartElement("error");
				out.writeAttribute("code",
						OAIErrorCode.toXmlString(error.getCode()));
				out.writeCharacters(error.getMessage());
				out.writeEndElement(); // error element
			}
			out.close();
		}
	}

	public static OAIProvider instance() {
		return s_instance;
	}

} // class OAIProvider
