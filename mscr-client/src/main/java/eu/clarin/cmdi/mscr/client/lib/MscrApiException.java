package eu.clarin.cmdi.mscr.client.lib;

/**
 * Unkown (not valid, not 4xx and not 5xx)
 * 
 * @author wilelb
 */
public class MscrApiException extends Exception {
    private int httpCode;
    /*
    public MscrApiException(String msg) {
        super(msg);
    }
    */
    public MscrApiException(int httpCode, String msg) {
        super(msg);
        this.httpCode = httpCode;
    }
    
    public MscrApiException(int httpCode, String msg, Throwable t) {
        super(msg, t);
        this.httpCode = httpCode;
    }

    /**
     * @return the httpCode
     */
    public int getHttpCode() {
        return httpCode;
    }
}
