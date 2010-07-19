package eu.clarin.cmdi.virtualcollectionregistry.oai.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;

public abstract class Verb {
    protected static final Logger logger = LoggerFactory.getLogger(Verb.class);

    public abstract String getName();

    public abstract Argument[] getArguments();

    public abstract void process(VerbContext ctx) throws OAIException;

    public final boolean supportsArgument(String name) {
        return getArgument(name) != null;
    }

    public final Argument getArgument(String name) {
        final Argument[] arguments = getArguments();
        if (arguments != null) {
            for (Argument argument : arguments) {
                if (argument.getName().equals(name)) {
                    return argument;
                }
            }
        }
        return null;
    }

} // abstract class Verb
