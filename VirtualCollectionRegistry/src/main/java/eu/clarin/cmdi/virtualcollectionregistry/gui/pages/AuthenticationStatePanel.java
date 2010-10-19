package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

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
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    } // private class LoginFragment

    private class LogoutFragment extends Fragment {
        public LogoutFragment(String id) {
            super(id, "logoutFragment", AuthenticationStatePanel.this);
            setRenderBodyOnly(true);
        }

        @Override
        public boolean isVisible() {
            return false;
        }
    } // private class LogoutFragment

} // class AuthenticationStatePanel
