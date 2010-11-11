package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;

import eu.clarin.cmdi.virtualcollectionregistry.gui.wizard.CreateVirtualCollectionWizard;

@AuthorizeInstantiation(Roles.USER)
public class CreateVirtualCollectionPage extends BasePage {

    public CreateVirtualCollectionPage() {
        super();
        this.add(new CreateVirtualCollectionWizard("wizard"));
    }
    
} // class CreateVirtualCollecionPage
