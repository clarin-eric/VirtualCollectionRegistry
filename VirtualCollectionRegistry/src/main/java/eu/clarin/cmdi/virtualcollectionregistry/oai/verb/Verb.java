package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Verb {
	protected static final Logger logger = LoggerFactory.getLogger(Verb.class);

	public abstract String getName();

	public abstract List<Argument> getArguments();

	public abstract void process(VerbContext ctx);
	
} // abstract class VerbBase
