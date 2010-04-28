package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;

public abstract class Verb {
	protected static final Logger logger = LoggerFactory.getLogger(Verb.class);

	public abstract String getName();

	public abstract List<Argument> getArguments();

	public abstract void process(VerbContext ctx) throws OAIException;

	public boolean supportsArgument(String name) {
		return getArgument(name) != null;
	}

	public Argument getArgument(String name) {
		List<Argument> arguments = getArguments();
		if (!arguments.isEmpty()) {
			for (Argument argument : arguments) {
				if (argument.getName().equals(name)) {
					return argument;
				}
			}
		}
		return null;
	}

} // abstract class Verb
