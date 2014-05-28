package eu.clarin.cmdi.virtualcollectionregistry.gui.auth;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

/**
 *
 * @deprecated no need for custom authentication
 * @see BasicAuthStrategy
 * @see ShibbolethAuthStrategy
 */
@Deprecated
public final class AuthFilter implements Filter {

    private final class RequestWrapper extends HttpServletRequestWrapper {

        private final AuthStrategy.Result result;

        public RequestWrapper(HttpServletRequest request,
                AuthStrategy.Result result) {
            super(request);
            this.result = result;
        }

        @Override
        public String getAuthType() {
            return result.isAuthenticated() ? strategy.getAuthType() : null;
        }

        @Override
        public Principal getUserPrincipal() {
            return result.isAuthenticated() ? result.getPrincipal() : null;
        }

        @Override
        public String getRemoteUser() {
            return result.isAuthenticated()
                    ? result.getPrincipal().getName()
                    : null;
        }
    } // class RequestWrapper

    private static final class ResponseWrapper
            extends HttpServletResponseWrapper {

        private boolean authRequested = false;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);

        }

        @Override
        public void setStatus(int sc, String sm) {
            if (sc == HttpServletResponse.SC_UNAUTHORIZED) {
                authRequested = true;
            }
            super.setStatus(sc, sm);
        }

        @Override
        public void setStatus(int sc) {
            if (sc == HttpServletResponse.SC_UNAUTHORIZED) {
                authRequested = true;
            }
            super.setStatus(sc);
        }

        public boolean isAuthRequested() {
            return authRequested;
        }
    } // class ResponseWrapper

    private static final String CONFIG_PARAM_AUTH_STRATEGY
            = "authfilter.strategy";
    private static final String STRATEGY_BASIC = "basic";
    private static final String STRATEGY_SHIBBOLETH = "shibboleth";
    private static final String AUTH_ACTION_PARAM = "authAction";
    private static final String AUTH_ACTION_LOGIN = "LOGIN";
    private static final String SESSION_PARAM_FORCED_AUTH = "authfilter.force";
    private AuthStrategy strategy;

    @Override
    public void init(FilterConfig config) throws ServletException {
        Map<String, String> cfg = new HashMap<String, String>();
        Enumeration<?> i = config.getServletContext().getInitParameterNames();
        while (i.hasMoreElements()) {
            String name = (String) i.nextElement();
            String value = config.getServletContext().getInitParameter(name);
            if ((value != null) && !value.isEmpty()) {
                cfg.put(name, value);
            }
        }
        String s = cfg.get(CONFIG_PARAM_AUTH_STRATEGY);
        if (s != null) {
            if (STRATEGY_BASIC.equalsIgnoreCase(s)) {
                strategy = new BasicAuthStrategy();
            } else if (STRATEGY_SHIBBOLETH.equalsIgnoreCase(s)) {
                strategy = new ShibbolethAuthStrategy();
            } else {
                throw new UnavailableException("invalid value for init "
                        + "parameter '" + CONFIG_PARAM_AUTH_STRATEGY
                        + "' (" + s + ")");
            }
            try {
                strategy.init(config, cfg);
            } catch (ServletException e) {
                throw new UnavailableException("error initalizing auth "
                        + "filter: " + e.getMessage());
            }
        } else {
            throw new UnavailableException("missing init parameter '"
                    + CONFIG_PARAM_AUTH_STRATEGY + "'");
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request0, ServletResponse response0,
            FilterChain chain) throws IOException, ServletException {
        if (strategy == null) {
            throw new UnavailableException("no auth strategy configured");
        }
        final HttpServletRequest request = (HttpServletRequest) request0;
        final HttpServletResponse response = (HttpServletResponse) response0;
        final HttpSession session = request.getSession();

        /*
         * Forced-Auth is a two step process: check if authAction query
         * parameter is set and if so, set an internal cookie and redirect
         * to the same URL without the authAction query parameter. On the
         * second request, ask the AuthStrategy to request authentication.
         * This is needed, to avoid an request auth loop, because the query
         * parameter was never deleted.
         */
        String action = request.getParameter(AUTH_ACTION_PARAM);
        if (action != null) {
            if (AUTH_ACTION_LOGIN.equalsIgnoreCase(action)) {
                session.setAttribute(SESSION_PARAM_FORCED_AUTH, Boolean.TRUE);
                StringBuffer url = request.getRequestURL();
                boolean firstParam = true;
                Iterator<?> params
                        = request.getParameterMap().entrySet().iterator();
                while (params.hasNext()) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, String[]> entry
                            = (Map.Entry<String, String[]>) params.next();
                    if (AUTH_ACTION_PARAM.equals(entry.getKey())) {
                        continue;
                    }
                    for (String value : entry.getValue()) {
                        if (firstParam) {
                            url.append('?');
                            firstParam = false;
                        } else {
                            url.append('&');
                        }
                        url.append(entry.getKey()).append('=').append(value);
                    }
                }
                response.sendRedirect(url.toString());
                return;
            }
        }
        if (session.getAttribute(SESSION_PARAM_FORCED_AUTH) == Boolean.TRUE) {
            session.removeAttribute(SESSION_PARAM_FORCED_AUTH);
            strategy.requestAuth(request, response);
            return;
        }

        AuthStrategy.Result result
                = strategy.handleAuth(request, response);
        if (result == null) {
            throw new UnavailableException(
                    "auth strategy returned null result");
        }
        switch (result.getAction()) {
            case CONTINUE_AUTHENTICATED:
            /* FALL-TROUGH */
            case CONTINUE_UNAUTHENTICATED:
                final RequestWrapper request2
                        = new RequestWrapper(request, result);
                final ResponseWrapper response2
                        = new ResponseWrapper(response);
                chain.doFilter(request2, response2);
                /*
                 * lazy auth: if request returned a status of 401 (unauthorized),
                 * request strategy to perform authorization
                 */
                if (response2.isAuthRequested()) {
                    strategy.requestAuth(request2, response2);
                }
                break;
            case RETRY:
                strategy.requestAuth(request, response);
                break;
            case ABORT:
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Authorization failed");
                break;
            case ERROR:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Authorization error due to bad request");
        } // switch
    }

} // class AuthFilter
