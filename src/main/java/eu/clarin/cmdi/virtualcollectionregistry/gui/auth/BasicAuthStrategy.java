package eu.clarin.cmdi.virtualcollectionregistry.gui.auth;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @deprecated to be replaced with basic authentication mechanism at container
 * level (e.g. tomcat-users)
 */
@Deprecated
final class BasicAuthStrategy implements AuthStrategy {
    private enum HashMethod {
        PLAIN, MD5, SHA;

        public static HashMethod fromString(String s) {
            if ("PLAIN".equalsIgnoreCase(s)) {
                return HashMethod.PLAIN;
            } else if ("MD5".equalsIgnoreCase(s)) {
                return HashMethod.MD5;
            } else if ("SHA".equalsIgnoreCase(s)) {
                return HashMethod.SHA;
            } else {
                return null;
            }
        }
    } // enum Hash

    private static final class Entry {
        private final HashMethod type;
        private final String username;
        private final String password;
        private final Map<String, String> attributes;

        public Entry(HashMethod type, String username, String password, Map<String, String> attributes) {
            this.type = type;
            this.username = username;
            this.password = password;
            this.attributes = attributes;
        }

        public boolean checkPassword(String pw) {
            switch (type) {
                case PLAIN:
                    return this.password.equals(pw);
                case MD5:
                    final String hash1 =
                        DigestUtils.md5Hex(prepare(pw)).toLowerCase();
                    return this.password.equals(hash1);
                case SHA:
                    final String hash2 =
                        DigestUtils.shaHex(prepare(pw)).toLowerCase();
                    return this.password.equals(hash2);
            } // switch
            return false;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        private String prepare(String pw) {
            // FIXME: salt should include something random
            return new StringBuilder(this.username)
                .append(':')
                .append(pw)
                .toString();
        }
    } // class Entry

    private static final String CONFIG_PARAM_USERDB_FILE =
        "authfilter.basic.userdb";
    private final ReadWriteLock userDbLock =
        new ReentrantReadWriteLock(true);
    private final Map<String, Entry> userDb =
        new HashMap<String, Entry>();

    @Override
    public void init(FilterConfig filterConfig, Map<String, String> config)
            throws ServletException {
        String userdb = config.get(CONFIG_PARAM_USERDB_FILE);
        if ((userdb == null) || userdb.isEmpty()) {
            throw new ServletException("missing init parameter '" +
                    CONFIG_PARAM_USERDB_FILE + "'");
        }
        try {
            InputStream in = new FileInputStream(userdb);
            loadUserDatabase(in);
        } catch (IOException e) {
            throw new ServletException("error initializing user database", e);
        }
    }

    @Override
    public String getAuthType() {
        return HttpServletRequest.BASIC_AUTH;
    }

    @Override
    public void requestAuth(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      final String realm =
          "Basic realm=\"CLARIN Virtual Collection Registry\"";
      response.addHeader("WWW-Authenticate", realm);
    }

    @Override
    public Result handleAuth(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            authorization = authorization.trim();
            if (authorization.isEmpty()) {
                authorization = null;
            }
        }
        Result result = new Result();
        if (authorization != null) {
            int pos = authorization.indexOf(' ');
            if (pos != -1) {
                final String scheme = authorization.substring(0, pos).trim();
                if (scheme.equalsIgnoreCase("Basic")) {
                    final String credentials =
                        decodeBase64(authorization.substring(pos + 1).trim());
                    pos = credentials.indexOf(':');
                    if ((pos != -1) && (pos < credentials.length() - 2)) {
                        final String username = credentials.substring(0, pos);
                        final String password = credentials.substring(pos + 1);
                        if (checkCredential(username, password)) {
                            Map<String, String> attr = getAttributes(username);
                            // login successful
                            result.setPrinicpal(
                                    new AuthPrincipal(username, attr));
                            result.setAction(Action.CONTINUE_AUTHENTICATED);
                        } else {
                            // login failed, retry
                            result.setAction(Action.RETRY);
                        }
                    } else {
                        result.setAction(Action.ERROR);
                    }
                } else {
                    // unsupported auth method, retry
                    result.setAction(Action.RETRY);
                }
            } else {
                result.setAction(Action.ERROR);
            }
        }
        return result;
    }

    private String decodeBase64(String s) throws ServletException {
        try {
            byte[] bytes = Base64.decodeBase64(s);
            return new String(bytes, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new ServletException("unsupported encoding");
        }
    }

    private boolean checkCredential(String username, String password) {
        Entry entry = null;
        Lock lock = userDbLock.readLock();
        lock.lock();
        try {
            entry = userDb.get(username);
        } finally {
            lock.unlock();
        }
        if (entry != null) {
            return entry.checkPassword(password);
        }
        return false;
    }

    private Map<String, String> getAttributes(String username) {
        Entry entry = null;
        Lock lock = userDbLock.readLock();
        lock.lock();
        try {
            entry = userDb.get(username);
        } finally {
            lock.unlock();
        }
        if (entry != null) {
            return entry.getAttributes();
        }
        return null;

    }

    private void loadUserDatabase(InputStream in) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(in, "UTF-8"));
        Lock lock = userDbLock.writeLock();
        lock.lock();
        try {
            userDb.clear();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int pos = line.indexOf(':');
                if ((pos != -1) && (pos < (line.length() - 2))) {
                    String username = line.substring(0, pos).trim();
                    int pos2 = line.indexOf(':', pos + 1);
                    String password = null;
                    String attrs = null;
                    if ((pos2 != -1) && (pos2 < (line.length() -2))) {
                        password = line.substring(pos + 1, pos2).trim();
                        attrs = line.substring(pos2 + 1).trim();
                    } else {
                        password = line.substring(pos + 1).trim();
                    }

                    if ((username != null) && (password != null)) {
                        if (!username.isEmpty() && !password.isEmpty()) {
                            HashMethod method = HashMethod.PLAIN;
                            if (password.startsWith("{")) {
                                pos = password.indexOf('}', 1);
                                if ((pos != -1) &&
                                        (pos < password.length() - 2)) {
                                    method = HashMethod.fromString(password
                                            .substring(1, pos));
                                    password = password.substring(pos + 1)
                                            .toLowerCase();
                                }
                            }
                            if (method != null) {
                                Map<String, String> attributes = null;
                                if (attrs != null) {
                                    attributes = parseAttributes(attrs);
                                }
                                userDb.put(username, new Entry(method,
                                        username, password, attributes));
                                continue;
                            }
                        }
                    }
                }
                // FIXME: better logging?
                System.err.println("MALFORMED LINE: " + line);
            }
        } finally {
            lock.unlock();
        }
        reader.close();
    }

    private Map<String, String> parseAttributes(String s) {
        Map<String, String> attributes = null;
        String[] attrs = s.split("\\s*,\\s*");
        if ((attrs != null) && (attrs.length > 0)) {
            Pattern pattern = Pattern.compile("(\\S+)\\s*=\\s*\"([^\"]+)\"");
            for (String attr : attrs) {
                Matcher m = pattern.matcher(attr);
                if (m.matches()) {
                    if (attributes == null) {
                        attributes = new HashMap<String,String>(attrs.length);
                    }
                    attributes.put(m.group(1).toLowerCase(), m.group(2));
                } else {
                    System.err.println("MALFORMED ATTRIBUTE: " + attr);
                }
            }
        }
        return attributes;
    }

} // class BasicAuthStrategy
