package eu.clarin.cmdi.virtualcollectionregistry.query;

public class QueryException extends Exception {
	private static final long serialVersionUID = 1L;

	public QueryException(String msg) {
		this(msg, null);
	}

	public QueryException(String msg, Throwable cause) {
		super(msg, cause);
	}

} // class QueryException
