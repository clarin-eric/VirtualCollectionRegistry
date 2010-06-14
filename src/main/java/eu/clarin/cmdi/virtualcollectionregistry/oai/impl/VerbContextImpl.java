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
import java.util.StringTokenizer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.OAIRepository;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;

public class VerbContextImpl implements VerbContext {
    private static final int ENCODING_NONE    = 0x00;
    private static final int ENCODING_IDENITY = 0x01;
    private static final int ENCODING_DEFLATE = 0x02;
    private static final int ENCODING_GZIP    = 0x03;

    private static class ErrorImpl implements Error {
        private OAIErrorCode code;
        private String message;

        public ErrorImpl(OAIErrorCode code, String message) {
            super();
            this.code = code;
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
        this.request = request;
        this.response = response;
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
             i.hasNext();) {
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
        int bestEnc = ENCODING_NONE;
        String accept = request.getHeader("Accept-Encoding");
        if (accept != null) {
            float bestQvalue = 0.0f;
            boolean identityPresent = false;

            StringTokenizer tok = new StringTokenizer(accept, ",");
            while (tok.hasMoreTokens()) {
                String item = tok.nextToken();

                String enc = null;
                float qvalue = -1.0f;

                int pos = item.indexOf(';');
                if (pos != -1) {
                    enc = item.substring(0, pos).trim();
                    int pos2 = item.indexOf('=', pos + 1);
                    if (pos2 != -1) {
                        String tmp = item.substring(pos2 + 1).trim();
                        try {
                            float value = Float.parseFloat(tmp);
                            if (value >= 0.0f && value <= 1.0f) {
                                qvalue = value;
                            }
                        } catch (NumberFormatException e) {
                            /* IGNORE */
                        }
                    }
                } else {
                    enc = item.trim();
                    qvalue = 1.0f;
                }

                // check, if encoding/qvalue pair is well-formed
                if (checkEncoding(enc) && (qvalue >= 0.0f)) {
                    // special handling for identity encoding flag
                    if ("identity".equalsIgnoreCase(enc) && (qvalue > 0.0f)) {
                        identityPresent = true;
                    }

                    // rate current best encoding versus parsed encoding
                    if (qvalue > bestQvalue) {
                        if ("identity".equalsIgnoreCase(enc) || "*".equals(enc)) {
                            bestEnc = ENCODING_IDENITY;
                        } else if ("deflate".equalsIgnoreCase(enc)
                                && repository
                                        .isSupportingCompressionMethod(OAIRepository.COMPRESSION_METHOD_DEFLATE)) {
                            bestEnc = ENCODING_DEFLATE;
                        } else if ("gzip".equalsIgnoreCase(enc)
                                && repository
                                        .isSupportingCompressionMethod(OAIRepository.COMPRESSION_METHOD_GZIP)) {
                            bestEnc = ENCODING_GZIP;
                        } else {
                            /* skip unsupported encoding */
                            continue;
                        }
                        bestQvalue = qvalue;
                    }
                } else {
                    throw new OAIException("malformed Accept-Encoding header");
                }
            } // while

            /*
             * OAI Specification mandates that identity encoding is included in
             * Accept-Encoding header with a non-zero qvalue. (see Section
             * 3.1.3)
             */
            if (!identityPresent) {
                /*
                 * XXX: if we where being pedantic, we would signal an error
                 * here. However, for now, we just assume, that identity
                 * encoding can be understood by the harvester.
                 */
                // throw new OAIException("Accept-Encoding header must " +
                // "include \"identity\" encoding with non-zero " +
                // "qvalue (see OAI Specification section 3.1.3)");
                if (bestEnc == ENCODING_NONE) {
                    bestEnc = ENCODING_IDENITY;
                }
            }
        } else {
            bestEnc = ENCODING_IDENITY;
        }

        try {
            response.setStatus(status);
            response.setContentType("text/xml");
            response.setCharacterEncoding("utf-8");

            OutputStream out = null;
            switch (bestEnc) {
            case ENCODING_IDENITY:
                out = response.getOutputStream();
                break;
            case ENCODING_DEFLATE:
                response.addHeader("Content-Encoding", "deflate");
                out = new DeflaterOutputStream(response.getOutputStream());
                break;
            case ENCODING_GZIP:
                response.addHeader("Content-Encoding", "gzip");
                out = new GZIPOutputStream(response.getOutputStream(),
                                           response.getBufferSize());
                break;
            default:
                throw new OAIException("no valid response encoding");
            }
            return new OAIOutputStreamImpl(this, out);
        } catch (Exception e) {
            throw new OAIException("error creating output stream", e);
        }
    }

    private boolean checkEncoding(String encoding) {
        if ("*".equals(encoding)) {
            return true;
        }
        for (int i = 0; i < encoding.length(); i++) {
            if (!Character.isLetter(encoding.charAt(i))) {
                return false;
            }
        }
        return true;
    }

} // class VerbContextImpl
