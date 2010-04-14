package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.impl.VerbContextImpl;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.GetRecordVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.IdentifyVerb;
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
			for (Argument arg : verb.getArguments()) {
				String value = ctx.getParameter(arg.getNameAsString());
				if (value != null) {
					remaining.remove(arg.getNameAsString());
					if (ctx.isRepeatedParameter(arg.getNameAsString())) {
						ctx.addError(OAIErrorCode.BAD_ARGUMENT,
									 "OAI verb '" + verbName +
									 "' has repeated values for argument '" +
									 arg.getName() + "'");
					} else {
						if (arg.validateArgument(value)) {
							ctx.setArgument(arg.getName(), value);
						} else { 
							ctx.addError(OAIErrorCode.BAD_ARGUMENT,
										 "Value of argument '" +
										 arg.getName() + "' of OAI verb '" +
										 verbName + "' is invalid");
						}
					}
				} else {
					if (arg.isRequired()) {
						ctx.addError(OAIErrorCode.BAD_ARGUMENT,
									 "OAI verb '" + verbName +
		                             "' is missing required argument '" +
		                             arg.getName() + "'");
					}
				}
			}  // for

			if (remaining.isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("processing verb '{}'", verb.getName());
					Map<Argument.Name, String> args = ctx.getArguments();
					if (!args.isEmpty()) {
						int i = 0;
						for (Argument.Name name : args.keySet()) {
							logger.debug("argument[" + i++ + "]: {}='{}'",
									name.toXmlString(), args.get(name));
						}
					}
				}
				verb.process(ctx);
			} else {
				logger.debug("received request with illegal arguments");
				for (String key : remaining) {
					ctx.addError(OAIErrorCode.BAD_ARGUMENT,
								 "OAI verb '" + verbName + "' was submitted " +
								 "with illegal argument '" + key + "' "+
								 "(value='" + ctx.getParameter(key) + "')");
				}
			}
		} else {
			ctx.addError(OAIErrorCode.BAD_VERB, "illegal OAI verb '" +
					verbName + "'");
		}

		if (ctx.hasErrors()) {
			OAIOutputStream out =
				ctx.getOutputStream(HttpServletResponse.SC_BAD_REQUEST);
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
