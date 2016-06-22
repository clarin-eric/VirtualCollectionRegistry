package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class AuthenticationStatePanel extends Panel {

    public AuthenticationStatePanel(String id) {
        super(id);
        setRenderBodyOnly(true);
        add(new LoginFragment("login"));
        add(new LogoutFragment("logout"));
    }

    private class LoginFragment extends Fragment {

        public LoginFragment(String id) {
            super(id, "loginFragment", AuthenticationStatePanel.this);
            setRenderBodyOnly(true);
            add(new Link("loginLink") {

                @Override
                public void onClick() {
                    setResponsePage(new DummyLoginPage(getPage().getPageReference()));
                }
            });
        }

        @Override
        public boolean isVisible() {
            return !((AuthenticatedWebSession) getSession()).isSignedIn();
        }

        @Override
        protected boolean getStatelessHint() {
            return true;
        }
    } // private class LoginFragment

    private class LogoutFragment extends Fragment {

        private final Label usernameLabel;

        public LogoutFragment(String id) {
            super(id, "logoutFragment", AuthenticationStatePanel.this);
            setRenderBodyOnly(true);
            usernameLabel = new Label("username");
            usernameLabel.setRenderBodyOnly(true);
            add(usernameLabel);
//            final StatelessLink logoutLink = new StatelessLink("logoutLink") {
//                @Override
//                public void onClick() {
//                }
//            };
//            add(logoutLink);
        }

        @Override
        public boolean isVisible() {
            return ((AuthenticatedWebSession) getSession()).isSignedIn();
        }

        @Override
        protected void onBeforeRender() {
            ApplicationSession session = (ApplicationSession) getSession();
            final Model<String> userModel
                    = new Model<String>(session.getUserDisplay());
            usernameLabel.setDefaultModel(userModel);
            super.onBeforeRender();
        }
    } // private class LogoutFragment

    @Override
    protected boolean getStatelessHint() {
        return true;
    }

} // class AuthenticationStatePanel
