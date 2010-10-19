package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.wizard.CreateVirtualCollectionWizard;

public class CreateVirtualCollectionPage extends BasePage {

    public CreateVirtualCollectionPage() {
        super();
        this.add(new CreateVirtualCollectionWizard("wizard"));
    }
    
} // class CreateVirtualCollecionPage
