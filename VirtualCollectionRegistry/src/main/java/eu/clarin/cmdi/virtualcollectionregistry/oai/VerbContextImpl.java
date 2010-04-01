package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.VerbContext;

public class VerbContextImpl implements VerbContext {
	private static class ErrorImpl implements Error {
		private OAIErrorCode code;
		private String message;
		
		public ErrorImpl(OAIErrorCode code, String message) {
			super();
			this.code    = code;
			this.message = message;
		}

		@Override
		public OAIErrorCode getCode() {
			return code;
		}

		@Override
		public String getMessage() {
			return message;
		}
	} // class ErrorImpl
	private final OAIProvider provider;
	private final ServletRequest request;
	private final ServletResponse response;
	private List<Error> errors = null;

	VerbContextImpl(OAIProvider provider, ServletRequest request, ServletResponse response) {
		super();
		this.provider = provider;
		this.request  = request;
		this.response = response;
	}

	@Override
	public OAIProvider getProvider() {
		return provider;
	}

	@Override
	public String getArgument(String name) {
		if (name != null) {
			String value = request.getParameter(name);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	@Override
	public void addError(OAIErrorCode code, String message) {
		if (errors == null) {
			errors = new ArrayList<Error>();
		}
		errors.add(new ErrorImpl(code, message));
	}

	@Override
	public boolean hasErrors() {
		return (errors != null);
	}

	@Override
	public List<Error> getErrors() {
		if (errors != null) {
			return errors;
		}
		return Collections.emptyList();
	}

} // class VerbContextImpl
