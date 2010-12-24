package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;

public class LoginPage extends WebPage {

    public LoginPage() {
        super();
        setStatelessHint(true);
        setVersioned(false);
    }

    @Override
    protected void onBeforeRender() {
        final HttpServletRequest request =
            getWebRequestCycle().getWebRequest().getHttpServletRequest();
        final HttpServletResponse response =
            getWebRequestCycle().getWebResponse().getHttpServletResponse();
        if (request.getAuthType() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            final Principal principal = request.getUserPrincipal();
            ApplicationSession session = (ApplicationSession) getSession();
            if (session.signIn(principal)) {
                if (!continueToOriginalDestination()) {
                    throw new RestartResponseAtInterceptPageException(
                            Application.get().getHomePage());
                }
            } else {
                throw new RestartResponseException(
                        Application.get().getApplicationSettings()
                            .getAccessDeniedPage());
            }
        }
        super.onBeforeRender();
    }

} // class LoginPage
