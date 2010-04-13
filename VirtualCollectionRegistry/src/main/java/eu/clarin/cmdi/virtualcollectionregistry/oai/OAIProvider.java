package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.ArrayList;
import java.util.List;
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

	private OAIProvider() {
		super();
		verbs = new ArrayList<Verb>();
		verbs.add(new IdentifyVerb());
		verbs.add(new GetRecordVerb());
	}

	public void process(VerbContextImpl ctx) throws OAIException {
		Verb verb = null;
		
		String verbName = ctx.getParameter("verb");
		if (verbName != null) {
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
			// set verb
			ctx.setVerb(verbName);

			// process arguments
			Set<String> remaining = ctx.getParameterNames();
			for (Argument arg : verb.getArguments()) {
				String value = ctx.getParameter(arg.getName());
				if ((value == null) && arg.isRequired()) {
					ctx.addError(OAIErrorCode.BAD_ARGUMENT,
								 "OAI verb '" + verbName +
	                             "' is missing required argument '" +
	                             arg.getName() + "'");
				} else {
					remaining.remove(arg.getName());
					if (ctx.isRepeatedParameter(arg.getName())) {
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
				}
			}  // for

			if (remaining.isEmpty()) {
				logger.debug("processing verb '{}'", verb.getName());
				verb.process(ctx);
			} else {
				for (String key : remaining) {
					ctx.addError(OAIErrorCode.BAD_ARGUMENT,
								 "OAI verb '" + verbName + "' was submitted " +
								 "with illegal argument '" + key + "' "+
								 "(value='" + ctx.getParameter(key) + "')");
				}
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
		} else {
			ctx.addError(OAIErrorCode.BAD_VERB, "illegal OAI verb '" +
					verbName + "'");
		}
	}

	public static OAIProvider instance() {
		return s_instance;
	}

} // class OAIProvider
