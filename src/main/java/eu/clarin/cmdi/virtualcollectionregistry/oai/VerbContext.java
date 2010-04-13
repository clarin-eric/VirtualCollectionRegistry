package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.List;
import java.util.Map;


public interface VerbContext {
	public static interface Error {
		public OAIErrorCode getCode();
		
		public String getMessage();
	} // interface Error

	public OAIProvider getProvider();

	public String getVerb();

	public String getArgument(String name);

	public Map<String, String> getArguments();

	public String getRequestURI();

	public void addError(OAIErrorCode code, String message);

	public boolean hasErrors();

	public List<Error> getErrors();

	public abstract OAIOutputStream getOutputStream()
		throws OAIException;

	public abstract OAIOutputStream getOutputStream(int status)
		throws OAIException;

} // interface VerbContext
