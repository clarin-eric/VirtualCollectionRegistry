package eu.clarin.cmdi.virtualcollectionregistry.gui.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @deprecated to be replaced with SHHAA filter
 */
@Deprecated
final class ShibbolethAuthStrategy implements AuthStrategy {

    private static final String SHIB_PARAM_RETURN = "return";
    private static final String SHIB_PARAM_TARGET = "target";
    private static final String SHIB_PARAM_PROVIDER = "providerId";
    private static final String CONFIG_PARAM_SSO
            = "authfilter.shibboleth.sso";
    private static final String CONFIG_PARAM_SLO
            = "authfilter.shibboleth.slo";
    private static final String CONFIG_PARAM_PROVIDER
            = "authfilter.shibboleth.provider";
    private static final String CONFIG_PARAM_HOST
            = "authfilter.shibboleth.host";
    private static final String CONFIG_PARAM_CONTEXT
            = "authfilter.shibboleth.context";
    private static final String CONFIG_PARAM_SESSION
            = "authfilter.shibboleth.session";
    private static final String CONFIG_PARAM_IDP
            = "authfilter.shibboleth.idp";
    private static final String CONFIG_PARAM_TIMESTAMP
            = "authfilter.shibboleth.timestamp";
    private static final String CONFIG_PARAM_USERNAME
            = "authfilter.shibboleth.username";
    private static final String CONFIG_PARAM_ATTRIBUTES
            = "authfilter.shibboleth.attributes";
    private static final String SESSION_PARAM_SID = "shib.sid";
    private static final String SESSION_PARAM_IDP = "shib.idp";
    private static final String SESSION_PARAM_TIMESTAMP = "shib.timestamp";
    private static final String SESSION_PARAM_PRINCIPAL = "shib.principal";
    private String ssoUrl;
    private String sloUrl;
    private String provider;
    private String host;
    private String context;
    private String sessionHeaderName;
    private String idpHeaderName;
    private String timestampHeaderName;
    private String[] usernameHeaderNames;
    private String[] attributeHeaderNames;

    @Override
    public void init(FilterConfig filterconfig, Map<String, String> config)
            throws ServletException {
        loadConfig(config);
    }

    @Override
    public String getAuthType() {
        return "SHIB";
    }

    @Override
    public void requestAuth(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        response.sendRedirect(makeSsoUrl(request));
    }

    @Override
    public Result handleAuth(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        Result result = new Result();

        HttpSession session = request.getSession();
        String oldSid = (String) session.getAttribute(SESSION_PARAM_SID);
        String sid = getHeader(request, sessionHeaderName);

        if (sid != null) {
            if ((oldSid == null) || !sid.equals(oldSid)) {
                /*
                 * FIXME: if sid != oldSid, shib session had was
                 *        logout or expired; pass that information on
                 */
                final AuthPrincipal principal
                        = refreshPrinicpal(request, session, sid);
                if (principal != null) {
                    result.setAction(Action.CONTINUE_AUTHENTICATED);
                    result.setPrinicpal(principal);
                }
            } else if (sid.equals(oldSid)) {
                final AuthPrincipal principal
                        = (AuthPrincipal) session.getAttribute(SESSION_PARAM_PRINCIPAL);
                result.setAction(Action.CONTINUE_AUTHENTICATED);
                result.setPrinicpal(principal);
            }
        }
        return result;
    }

    private AuthPrincipal refreshPrinicpal(HttpServletRequest request,
            HttpSession session, String sid) {
        final String idp = getHeader(request, idpHeaderName);
        final String timestamp = getHeader(request, timestampHeaderName);
        final String username = getFirstHeader(request,
                usernameHeaderNames);
        if ((username != null) && (idp != null) && (timestamp != null)) {
            Map<String, String> attributes = null;
            if (attributeHeaderNames != null) {
                for (String name : attributeHeaderNames) {
                    String value = getHeader(request, name);
                    if (value != null) {
                        if (attributes == null) {
                            attributes = new HashMap<String, String>();
                        }
                        attributes.put(name.toLowerCase(), value);
                    }
                }
            }
            AuthPrincipal principal = new AuthPrincipal(username, attributes);
            session.setAttribute(SESSION_PARAM_SID, sid);
            session.setAttribute(SESSION_PARAM_IDP, idp);
            session.setAttribute(SESSION_PARAM_TIMESTAMP, timestamp);
            session.setAttribute(SESSION_PARAM_PRINCIPAL, principal);
            return principal;
        }
        return null;
    }

    private String getHeader(HttpServletRequest request, String name) {
        String s = request.getHeader(name);
        if (s != null) {
            s = s.trim();
            if (s.isEmpty()) {
                s = null;
            }
        }
        return s;
    }

    private String getFirstHeader(HttpServletRequest request, String[] names) {
        for (String name : names) {
            String s = getHeader(request, name);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    private String makeSsoUrl(HttpServletRequest request) {
        return makeUrl(ssoUrl, SHIB_PARAM_TARGET, request);
    }

    @SuppressWarnings("unused")
    private String makeSloUrl(HttpServletRequest request) {
        return makeUrl(sloUrl, SHIB_PARAM_RETURN, request);
    }

    private String makeUrl(String shibUrl, String param, HttpServletRequest request) {
        StringBuilder target = new StringBuilder();
        target.append(request.getScheme());
        target.append("://");
        if (host != null) {
            target.append(host);
        } else {
            final String scheme = request.getScheme();
            int port = request.getServerPort();
            if (port < 0) {
                port = 80;
            }
            target.append(request.getServerName());
            if ((scheme.equalsIgnoreCase("http") && (port != 80))
                    || (scheme.equalsIgnoreCase("https") && (port != 443))) {
                target.append(':');
                target.append(port);
            }
        }
        if (context != null) {
            target.append(context);
        } else {
            target.append(request.getContextPath());
        }
        String path = request.getServletPath();
        if ((path != null) && !path.isEmpty()) {
            target.append(path);
        }
        // handle QueryParams
        boolean firstParam = true;
        Iterator<?> params = request.getParameterMap().entrySet().iterator();
        while (params.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, String[]> entry
                    = (Map.Entry<String, String[]>) params.next();
            for (String value : entry.getValue()) {
                if (firstParam) {
                    target.append('?');
                    firstParam = false;
                } else {
                    target.append('&');
                }
                target.append(entry.getKey()).append('=').append(value);
            }
        }

        StringBuilder url = new StringBuilder(shibUrl);
        url.append('?')
                .append(param)
                .append('=')
                .append(urlEncode(target.toString()));
        if (SHIB_PARAM_TARGET.equals(param) && (provider != null)) {
            url.append('&')
                    .append(SHIB_PARAM_PROVIDER)
                    .append('=')
                    .append(provider);
        }
        return url.toString();
    }

    private void loadConfig(Map<String, String> cfg) {
        ssoUrl = readProperty(cfg, CONFIG_PARAM_SSO, "sso");
        sloUrl = readProperty(cfg, CONFIG_PARAM_SLO, "slo");
        provider = readProperty(cfg, CONFIG_PARAM_PROVIDER, null);
        host = readProperty(cfg, CONFIG_PARAM_HOST, null);
        context = readProperty(cfg, CONFIG_PARAM_CONTEXT, null);
        sessionHeaderName
                = readProperty(cfg, CONFIG_PARAM_SESSION, "Shib-Session-ID");
        idpHeaderName
                = readProperty(cfg, CONFIG_PARAM_IDP, "Shib-Identity-Provider");
        timestampHeaderName = readProperty(cfg, CONFIG_PARAM_TIMESTAMP,
                "Shib-Authentication-Instant");
        usernameHeaderNames = readProperties(cfg, CONFIG_PARAM_USERNAME,
                new String[]{
                    "eduPersonPrincipalName",
                    "eppn"
                });
        attributeHeaderNames
                = readProperties(cfg, CONFIG_PARAM_ATTRIBUTES, null);
    }

    private static String readProperty(Map<String, String> cfg, String name,
            String defaulValue) {
        String s = cfg.get(name);
        if (s != null) {
            s = s.trim();
            if (s.isEmpty()) {
                s = null;
            }
        }
        return (s != null) ? s : defaulValue;
    }

    private static String[] readProperties(Map<String, String> cfg,
            String name, String[] defaultValue) {
        String s = readProperty(cfg, name, null);
        if (s != null) {
            String[] result = s.split("[,\\s]+");
            if (result.length > 0) {
                return result;
            }
        }
        return defaultValue;
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("bad encoding", e);
        }
    }

} // class ShibbolethAuthStrategy
