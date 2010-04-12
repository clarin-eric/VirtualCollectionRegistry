package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.io.Writer;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIProvider;

public interface VerbContext {
	public static interface Error {
		public OAIErrorCode getCode();
		
		public String getMessage();
	} // interface Error

	public OAIProvider getProvider();

	public String getArgument(String name);

	public String getRequestURI();

	public void addError(OAIErrorCode code, String message);

	public boolean hasErrors();

	public List<Error> getErrors();

	public Writer getWriter();

} // interface VerbContext
