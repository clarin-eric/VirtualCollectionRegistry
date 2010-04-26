package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.List;
import java.util.Map;


public interface VerbContext {
	public static interface Error {
		public OAIErrorCode getCode();
		
		public String getMessage();
	} // interface Error

	public OAIRepositoryAdapter getRepository();

	public String getVerb();

	// XXX: add method for fetching request timestamp

	public boolean hasArgument(String name);

	public Object getArgument(String name);

	public Map<String, String> getUnparsedArguments();

	public String getContextPath();

	public String getRequestURI();

	public void addError(OAIErrorCode code, String message);

	public boolean hasErrors();

	public List<Error> getErrors();

	public OAIOutputStream getOutputStream()
		throws OAIException;

	public OAIOutputStream getOutputStream(int status)
		throws OAIException;

} // interface VerbContext
