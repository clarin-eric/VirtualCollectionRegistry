package eu.clarin.cmdi.oai.provider.ext;

import java.util.List;
import java.util.Map;

import eu.clarin.cmdi.oai.provider.OAIException;

public interface VerbContext {
    public interface Error {
        public OAIErrorCode getCode();

        public String getMessage();
    } // interface Error

    public RepositoryAdapter getRepository();

    public String getVerb();

    public boolean hasArgument(String name);

    public Object getArgument(String name);

    public Map<String, String> getUnparsedArguments();

    public String getContextPath();

    public String getRequestURI();

    public void addError(OAIErrorCode code, String message);

    public boolean hasErrors();

    public List<Error> getErrors();

    public OAIOutputStream getOutputStream() throws OAIException;

} // interface VerbContext
