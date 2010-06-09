package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.impl.VerbContextImpl;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.OAIRepository;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.GetRecordVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.IdentifyVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.ListIdentifiersVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.ListMetadataFormatsVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.ListRecordsVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.ListSetsVerb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Verb;

public class OAIProvider {
	private static final Logger logger =
		LoggerFactory.getLogger(OAIProvider.class);
	private static final OAIProvider s_instance = new OAIProvider();
	private final List<Verb> verbs;
	private final Map<String, ResumptionToken> resumptionTokens =
		new HashMap<String, ResumptionToken>(64);
	private Timer timer = new Timer("OAI-Provider-Maintenance", true);
	private AtomicBoolean isAvailable = new AtomicBoolean();
	private OAIRepositoryAdapter repository;
	
	private OAIProvider() {
		super();
		verbs = new ArrayList<Verb>();
		verbs.add(new IdentifyVerb());
		verbs.add(new ListMetadataFormatsVerb());
		verbs.add(new ListIdentifiersVerb());
		verbs.add(new ListSetsVerb());
		verbs.add(new ListRecordsVerb());
		verbs.add(new GetRecordVerb());

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				synchronized (resumptionTokens) {
					if (resumptionTokens.isEmpty()) {
						return;
					}
					Iterator<ResumptionToken> i =
						resumptionTokens.values().iterator();
					while (i.hasNext()) {
						ResumptionToken token = i.next();
						synchronized (token) {
							if (token.checkExpired(scheduledExecutionTime())) {
								i.remove();
								System.err.println("expire: " + token.getId());
							}
						} // synchronized (token)
					} // while
				} // synchronized (resumptionTokens)
			}
		}, 60000, 60000);
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
		if (!isAvailable.compareAndSet(false, true)) {
			throw new IllegalStateException("unexpected state of isAvailable");
		}
	}

	public boolean isAvailable() {
		return isAvailable.get();
	}

	public void setIsAvailable(boolean value) {
		if (repository != null) {
			isAvailable.set(value);
		}
	}

	public void shutdown() {
		timer.cancel();
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

			if (verb.supportsArgument(Argument.ARG_RESUMPTIONTOKEN) &&
				remaining.contains(Argument.ARG_RESUMPTIONTOKEN)) {
				// special handling of resumptionToken
				String value = ctx.getParameter(Argument.ARG_RESUMPTIONTOKEN);
				if (value != null) {
					Argument arg =
						verb.getArgument(Argument.ARG_RESUMPTIONTOKEN);
					remaining.remove(arg.getName());
					if (ctx.isRepeatedParameter(arg.getName())) {
						ctx.addError(OAIErrorCode.BAD_ARGUMENT, "OAI verb '" +
								verb.getName() +
								"' has repeated values for argument '" +
								arg.getName() + "'");
					} else {
						if (!ctx.setArgument(arg, value)) {
							ctx.addError(OAIErrorCode.BAD_ARGUMENT,
									"Value of argument '" +
									arg.getName() +
									"' of OAI verb '" +
									verb.getName() +
									"' is invalid (value='" + value + "')");
						}
					}
				}
			} else {
				// process regular arguments
				for (Argument arg : verb.getArguments()) {
					String value = ctx.getParameter(arg.getName().toString());
					if (value != null) {
						remaining.remove(arg.getName());
						if (ctx.isRepeatedParameter(arg.getName())) {
							ctx.addError(OAIErrorCode.BAD_ARGUMENT,
										 "OAI verb '" + verb.getName() +
										 "' has repeated values for " +
										 "argument '" + arg.getName() + "'");
						} else {
							if (!ctx.setArgument(arg, value)) {
								ctx.addError(OAIErrorCode.BAD_ARGUMENT,
											 "Value of argument '" +
											 arg.getName() +
											 "' of OAI verb '" +
											 verb.getName() +
											 "' is invalid (value='" +
											 value + "')");
							}
						}
					} else {
						if (arg.isRequired()) {
							ctx.addError(OAIErrorCode.BAD_ARGUMENT,
										 "OAI verb '" + verb.getName() +
			                             "' is missing required argument '" +
			                             arg.getName() + "'");
						}
					}
				}  // for
			}

			if (!remaining.isEmpty()) {
				logger.debug("received request with illegal arguments");
				for (String key : remaining) {
					ctx.addError(OAIErrorCode.BAD_ARGUMENT,
								 "OAI verb '" + verb.getName()+
								 "' was submitted with illegal argument '" +
								 key + "' (value='" + ctx.getParameter(key) +
								 "')");
				}
			}

			/*
			 * Execute verb, if no error occurred have been recorded.
			 */
			if (!ctx.hasErrors()) {
				if (logger.isDebugEnabled()) {
					logger.debug("processing verb '{}'", verb.getName());
					Map<String, String> args = ctx.getUnparsedArguments();
					if (!args.isEmpty()) {
						int i = 0;
						for (String name : args.keySet()) {
							logger.debug("argument[" + i++ + "]: {}='{}'",
									name, args.get(name));
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

	ResumptionToken createResumptionToken(long lifetime) {
		long expirationDate = System.currentTimeMillis() +
			((lifetime > 0) ? lifetime : 600000);
		ResumptionToken token = new ResumptionToken();
		token.setExpirationDate(expirationDate);
		synchronized (resumptionTokens) {
			resumptionTokens.put(token.getId(), token);
		} // synchronized (resumptionTokens)
		return token;
	}

	ResumptionToken getResumptionToken(String id, long lifetime) {
		synchronized (resumptionTokens) {
			ResumptionToken token = resumptionTokens.get(id);
			if (token == null) {
				return null;
			}
			synchronized (token) {
				long expirationDate = System.currentTimeMillis() +
					((lifetime > 0) ? lifetime : 600000);
				token.setExpirationDate(expirationDate);
				return token;
			} // synchronized (token)
		} // synchronized (resumptionTokens)
	} 

} // class OAIProvider
