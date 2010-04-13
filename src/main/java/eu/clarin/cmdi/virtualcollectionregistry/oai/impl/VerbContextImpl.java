package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

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

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIProvider;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;


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
	private String verb = null;
	private Map<String, String> arguments = null;
	private List<Error> errors = null;

	VerbContextImpl(OAIProvider provider,
	  			   HttpServletRequest request,
	  			   HttpServletResponse response) {
		this.provider = provider;
		this.request = request;
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

	public boolean isRepeatedParameter(String name) {
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
	
	public void setVerb(String verb) {
		this.verb = verb;
	}

	public void setArgument(String name, String value) {
		if ((name == null) || (value == null)) {
			throw new NullPointerException("name == null || value == null");
		}
		if (arguments == null) {
			arguments = new HashMap<String, String>();
		}
		arguments.put(name, value);
	}

	@Override
	public String getVerb() {
		return verb;
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
		return value;
	}

	@Override
	public Map<String, String> getArguments() {
		if (arguments == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(arguments);
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
		return request.getRequestURL().toString();
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
	public OAIOutputStream getOutputStream() throws OAIException {
		return this.getOutputStream(HttpServletResponse.SC_OK);
	}

	@Override
	public OAIOutputStream getOutputStream(int status) throws OAIException {
		try {
			response.setStatus(status);
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/xml");
			return new OAIOutputStreamImpl(this, response.getOutputStream());
		} catch (Exception e) {
			throw new OAIException("error creating output stream", e);
		}
	}

} // class VerbContextImpl
