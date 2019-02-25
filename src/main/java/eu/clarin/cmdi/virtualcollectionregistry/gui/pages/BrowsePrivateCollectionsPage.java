package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PrivateCollectionsProvider;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

@AuthorizeInstantiation(Roles.USER)
public class BrowsePrivateCollectionsPage extends BasePage {

    public BrowsePrivateCollectionsPage() {
        add(new BrowseEditableCollectionsPanel("collections", new PrivateCollectionsProvider(), getPageReference()));
    }
    
}
