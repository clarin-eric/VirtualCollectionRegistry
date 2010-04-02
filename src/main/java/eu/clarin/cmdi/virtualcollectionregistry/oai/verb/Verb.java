package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;

public abstract class Verb {
	protected static final Logger logger = LoggerFactory.getLogger(Verb.class);

	public abstract String getName();

	protected abstract List<Argument> getArguments();

	protected abstract void doProcess(VerbContext ctx);

	public void process(VerbContext ctx) {
		/* FIXME: need to check for multiple arguments -> illegal
		 *        check for superfluous arguments      -> illegal
		 */
		for (Argument arg : getArguments()) {
			String value = ctx.getArgument(arg.getName());
			if ((value == null) && arg.isRequired()) {
				ctx.addError(OAIErrorCode.BAD_ARGUMENT,
								"OAI verb '" + getName() +
                                "' requires argument '" + arg.getName() + "'");
			} else {
				try {
					arg.validateArgument(value);
				} catch (OAIException e) { 
					ctx.addError(OAIErrorCode.BAD_ARGUMENT,
							"Value of argument '" +
							arg.getName() + "' of OAI verb '" +
							getName() + "' is invalid: " + e.getMessage());
				}
			}
		} // for
		if (!ctx.hasErrors()) {
			doProcess(ctx);
		}
	}

} // abstract class VerbBase
