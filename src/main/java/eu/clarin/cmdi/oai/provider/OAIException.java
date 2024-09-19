package eu.clarin.cmdi.oai.provider;

public class OAIException extends Exception {
    private static final long serialVersionUID = 1L;

    public OAIException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public OAIException(String msg) {
        this(msg, null);
    }

} // class OAIException
