package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

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
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private OAIRepositoryAdapter repository;
	private String verb;
	private Map<String, Object> arguments;
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

	public boolean setArgument(Argument arg, String value) {
		if (arg.checkArgument(value)) {
			Object v = repository.parseArgument(arg.getName(), value);
			if (v != null) {
				if (arguments == null) {
					arguments = new HashMap<String, Object>();
				}
				arguments.put(arg.getName(), v);
				return true;
			}
		}
		return false;
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
	public boolean hasArgument(String name) {
		boolean result = false;
		if (arguments != null) {
			result = arguments.containsKey(name);
		}
		return result;
	}


	@Override
	public Object getArgument(String name) {
		Object value = null;
		if (arguments != null) {
			value = arguments.get(name);
		}
		return value;
	}

	@Override
	public Map<String, String> getUnparsedArguments() {
		if (arguments == null) {
			return Collections.emptyMap();
		}
		Map<String, String> result =
			new HashMap<String, String>(arguments.size());
		for (String name : arguments.keySet()) {
			result.put(name, getParameter(name));
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
			response.setContentType("text/xml");
			response.setCharacterEncoding("utf-8");
			OutputStream out = null;
			String accept = request.getHeader("Accept-Encoding");
			if (accept != null) {
				/*
				 *  XXX: this is not totally correct as the qvalues are
				 *       currently ignored.
				 */
				if (accept.indexOf("gzip") != -1) {
					response.addHeader("Content-Encoding", "gzip");
					out = new GZIPOutputStream(response.getOutputStream(),
											   response.getBufferSize());
				} else if (accept.indexOf("deflate") != -1) {
					response.addHeader("Content-Encoding", "deflate");
					out = new DeflaterOutputStream(response.getOutputStream());
				}
			}
			if (out == null) {
				out = response.getOutputStream();
			}
			return new OAIOutputStreamImpl(this, out);
		} catch (Exception e) {
			throw new OAIException("error creating output stream", e);
		}
	}

} // class VerbContextImpl
