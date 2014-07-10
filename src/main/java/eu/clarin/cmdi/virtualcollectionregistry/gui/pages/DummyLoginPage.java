package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;

/**
 * Only serves as a target for the 'login' link of the
 * {@link AuthenticationStatePanel}
 *
 * @author twagoo
 */
@AuthorizeInstantiation(Roles.USER)
public class DummyLoginPage extends BasePage {

    private final PageReference pageRef;

    public DummyLoginPage(PageReference pageRef) {
        this.pageRef = pageRef;
    }

    @Override
    protected void onBeforeRender() {
        throw new RestartResponseAtInterceptPageException(pageRef.getPage());
    }

}
