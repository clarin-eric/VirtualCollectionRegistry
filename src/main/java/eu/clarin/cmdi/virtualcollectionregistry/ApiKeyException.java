package eu.clarin.cmdi.virtualcollectionregistry;

public class ApiKeyException extends Exception {
    public ApiKeyException(String msg) {
        super(msg);
    }

    public ApiKeyException(String msg, Throwable t) {
        super(msg, t);
    }
}
