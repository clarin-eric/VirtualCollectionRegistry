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
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;


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
	private class ArgumentValueImpl {
		private String value;
		private Object parsedValue;
		
		public ArgumentValueImpl(String value, Object parsedValue) {
			this.value       = value;
			this.parsedValue = parsedValue != null ? parsedValue : value;
		}
		
		public String getValue() {
			return value;
		}
		
		public Object getParsedValue() {
			return parsedValue;
		}
	} // class ArgumrntValueImpl
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private OAIRepositoryAdapter repository;
	private String verb;
	private Map<Argument.Name, ArgumentValueImpl> arguments;
	private List<Error> errors;

	VerbContextImpl(HttpServletRequest request, HttpServletResponse response) {
		this.request    = request;
		this.response   = response;
	}

	public void setRepository(OAIRepositoryAdapter repository) {
		this.repository = repository;
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

	public void setArgument(Argument.Name name, String value, Object parsedValue) {
		if ((name == null) || (value == null)) {
			throw new NullPointerException("name == null || value == null");
		}
		if (arguments == null) {
			arguments = new HashMap<Argument.Name, ArgumentValueImpl>();
		}
		arguments.put(name, new ArgumentValueImpl(value, parsedValue));
	}

	@Override
	public String getVerb() {
		return verb;
	}

	@Override
	public OAIRepositoryAdapter getRepository() {
		return repository;
	}

	@Override
	public boolean hasArgument(Argument.Name name) {
		boolean result = false;
		if (arguments != null) {
			result = arguments.containsKey(name);
		}
		return result;
	}

	@Override
	public String getUnparsedArgument(Argument.Name name) {
		if (arguments != null) {
			ArgumentValueImpl value = arguments.get(name);
			if (value != null) {
				return value.getValue();
			}
		}
		return null;
	}

	@Override
	public Object getParsedArgument(Argument.Name name) {
		if (arguments != null) {
			ArgumentValueImpl value = arguments.get(name);
			if (value != null) {
				return value.getParsedValue();
			}
		}
		return null;
	}

	@Override
	public Map<Argument.Name, String> getUnparsedArguments() {
		if (arguments == null) {
			return Collections.emptyMap();
		}
		Map<Argument.Name, String> result =
			new HashMap<Argument.Name, String>(arguments.size());
		for (Argument.Name key : arguments.keySet()) {
			ArgumentValueImpl value = arguments.get(key);
			result.put(key, value.getValue());
		}
		return result;
	}

	@Override
	public void addError(OAIErrorCode code, String message) {
		if (errors == null) {
			errors = new ArrayList<Error>();
		}
		errors.add(new ErrorImpl(code, message));
	}

	@Override
	public String getContextPath() {
		return request.getContextPath();
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
			response.setBufferSize(8192);
			return new OAIOutputStreamImpl(this, response.getOutputStream());
		} catch (Exception e) {
			throw new OAIException("error creating output stream", e);
		}
	}

} // class VerbContextImpl
