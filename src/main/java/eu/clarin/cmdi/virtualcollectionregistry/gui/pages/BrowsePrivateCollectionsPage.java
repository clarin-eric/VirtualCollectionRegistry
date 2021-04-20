package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PrivateCollectionsProvider;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation(Roles.USER)
public class BrowsePrivateCollectionsPage extends BasePage {

    private final static Logger logger = LoggerFactory.getLogger(BrowsePrivateCollectionsPage.class);

    public BrowsePrivateCollectionsPage() {
        final BrowseEditableCollectionsPanel pnl =
                new BrowseEditableCollectionsPanel("collections", new PrivateCollectionsProvider(), getPageReference());
        pnl.setOutputMarkupId(true);
        add(pnl);
    }
    
}
