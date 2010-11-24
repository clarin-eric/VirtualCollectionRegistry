package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.Page;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;

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
            final StatelessLink loginLink = new StatelessLink("loginLink") {
                @Override
                public void onClick() {
                }

                @Override
                protected CharSequence getURL() {
                    final Page page = getPage();
                    StringBuilder url =
                        new StringBuilder(urlFor(page.getClass(),
                                page.getPageParameters()));
                    if (url.indexOf("?") != -1) {
                        url.append('&');
                    } else {
                        url.append('?');
                    }
                    url.append("authAction=LOGIN");
                    return url.toString();
                }
            };
            add(loginLink);
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
            usernameLabel.setDefaultModel(new Model<String>(session.getUser()));
            super.onBeforeRender();
        }
    } // private class LogoutFragment

    @Override
    protected boolean getStatelessHint() {
        return true;
    }

} // class AuthenticationStatePanel
