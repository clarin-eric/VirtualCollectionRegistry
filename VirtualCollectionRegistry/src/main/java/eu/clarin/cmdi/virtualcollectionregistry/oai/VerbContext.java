package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.List;
import java.util.Map;

import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;


public interface VerbContext {
	public static interface Error {
		public OAIErrorCode getCode();
		
		public String getMessage();
	} // interface Error

	public OAIRepositoryAdapter getRepository();

	public String getVerb();

	// XXX: add method for fetching request timestamp

	public boolean hasArgument(Argument.Name name);

	public Object getArgument(Argument.Name name);

	public Map<Argument.Name,String> getUnparsedArguments();

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
