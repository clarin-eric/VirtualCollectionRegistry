package eu.clarin.cmdi.virtualcollectionregistry.gui.auth;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface AuthStrategy {
    public enum Action {
        CONTINUE_AUTHENTICATED, CONTINUE_UNAUTHENTICATED, RETRY, ABORT, ERROR;
    } // enum State

    public class Result {
        private Action action;
        private AuthPrincipal principal;
        
        protected Result() {
            this.action = Action.CONTINUE_UNAUTHENTICATED;
        }
        
        public final Action getAction() {
            return action;
        }
        
        public final void setAction(Action action) {
            if (action == null) {
                throw new IllegalArgumentException("action == null");
            }
            this.action = action;
        }

        public final boolean isAuthenticated() {
            return (this.action == Action.CONTINUE_AUTHENTICATED);
        }

        public AuthPrincipal getPrincipal() {
            return principal;
        }
        
        public void setPrinicpal(AuthPrincipal principal) {
            this.principal = principal;
        }
    } // class Result

    public void init(FilterConfig filterconfig, Map<String, String> config)
            throws ServletException;

    public String getAuthType();

    public void requestAuth(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException;

    public AuthStrategy.Result handleAuth(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException;

} // interface AuthStrategy
