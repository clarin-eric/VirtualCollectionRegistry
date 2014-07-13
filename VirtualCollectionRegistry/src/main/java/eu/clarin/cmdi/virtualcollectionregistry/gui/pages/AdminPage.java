package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;

@AuthorizeInstantiation(Roles.ADMIN)
@AuthorizeAction(action = "ENABLE", roles = { Roles.ADMIN })
public class AdminPage extends BasePage {

    public AdminPage() {
        super();
    }

} // class AdminPage
