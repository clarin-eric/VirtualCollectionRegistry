package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.markup.html.WebPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;

public class LoginPage extends WebPage {

    public LoginPage() {
        super();
        setStatelessHint(true);
        setVersioned(false);
    }

    @Override
    protected void onBeforeRender() {
        AuthenticationHandler.handleLogin((ApplicationSession) getSession(), this);
        super.onBeforeRender();
    }

} // class LoginPage
