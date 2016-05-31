package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import org.apache.wicket.request.cycle.RequestCycle;

public class LoginPage extends WebPage {

    public LoginPage() {
        super();
        setStatelessHint(true);
        setVersioned(false);
    }

    @Override
    protected void onBeforeRender() {
        final RequestCycle cycle =  RequestCycle.get();
        final HttpServletRequest request = 
            (HttpServletRequest)cycle.getRequest().getContainerRequest();
        final HttpServletResponse response = 
            (HttpServletResponse)cycle.getResponse().getContainerResponse();
        if (request.getAuthType() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            final Principal principal = request.getUserPrincipal();
            ApplicationSession session = (ApplicationSession) getSession();
            if (session.signIn(principal)) {
                continueToOriginalDestination();
                // if we reach this line there was no intercept page, so go to home page
                throw new RestartResponseAtInterceptPageException(
                    Application.get().getHomePage());
            } else {
                throw new RestartResponseException(
                        Application.get().getApplicationSettings()
                            .getAccessDeniedPage());
            }
        }
        super.onBeforeRender();
    }

} // class LoginPage
