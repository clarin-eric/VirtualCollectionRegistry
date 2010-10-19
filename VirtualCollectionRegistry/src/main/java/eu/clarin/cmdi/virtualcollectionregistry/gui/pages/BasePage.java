package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;

public class BasePage extends WebPage {

    public BasePage() {
        super();
        // authentication state
        add(new AuthenticationStatePanel("authstate"));

        // main navigation menu
        final Menu menu = new Menu("menu");
        menu.addMenuItem(new MenuItem<HomePage>(
                new Model<String>("Home"), 
                HomePage.class));
        menu.addMenuItem(new MenuItem<CreateVirtualCollectionPage>(
                new Model<String>("Create Virtual Collection"),
                CreateVirtualCollectionPage.class));
        add(menu);
        
        // add version to footer
        VirtualCollectionRegistry vcr = VirtualCollectionRegistry.instance();
        add(new Label("version", vcr.getVersion()).setRenderBodyOnly(true));
    }

} // class BasePage
