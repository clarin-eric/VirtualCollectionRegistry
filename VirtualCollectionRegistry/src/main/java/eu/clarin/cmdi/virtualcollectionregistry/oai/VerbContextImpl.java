package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private Map<String, String> arguments = null;
	private List<Error> errors = null;

	VerbContextImpl(OAIProvider provider,
				    HttpServletRequest request,
				    HttpServletResponse response) {
		super();
		this.provider = provider;
		this.request  = request;
		this.response = response;
	}

	public String getParameter(String name) {
		String value = request.getParameter(name);
		if (value != null) {
			value = value.trim();
			if (!value.isEmpty()) {
				return value;
			}
		}
		return null;
	}

	public boolean isParameterMultivalued(String name) {
		String[] params = request.getParameterValues(name);
		if (params != null) {
			return params.length > 1;
		}
		return false;
	}

	public Set<String> getParameterNames() {
		Set<String> names = new HashSet<String>();
		for (Iterator<?> i = request.getParameterMap().keySet().iterator();
		 	 i.hasNext(); ) {
			String s = (String) i.next();
			if (s.equalsIgnoreCase("verb")) {
				continue;
			}
			names.add(s);
		}
		return names;
	}
	
	public void setArgument(String name, String value) {
		if (arguments == null) {
			arguments = new HashMap<String, String>();
		}
		arguments.put(name, value);
	}

	@Override
	public OAIProvider getProvider() {
		return provider;
	}

	@Override
	public String getArgument(String name) {
		String value = null;
		if (arguments != null) {
			value = arguments.get(name);
		}
		if (value == null) {
			throw new NullPointerException("bad argument: value == null");
		}
		return value;
	}

	@Override
	public void addError(OAIErrorCode code, String message) {
		if (errors == null) {
			errors = new ArrayList<Error>();
		}
		errors.add(new ErrorImpl(code, message));
	}

	@Override
	public String getRequestURI() {
		// FIXME: supposed to return request uri
		return null;
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

	@Override
	public Writer getWriter() {
		// FIXME: this is for testing only, need to be re-factored
		try {
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return response.getWriter();
		} catch (IOException e) {
		}
		return null;
	}

} // class VerbContextImpl
