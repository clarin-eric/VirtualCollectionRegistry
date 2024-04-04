package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.core.AdminUsersService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class AdminPanel extends Panel {

    public AdminPanel(String id, final AdminUsersService adminUsersService) {
        super(id);

        ListView authorsListview = new ListView("admin_list", adminUsersService.getAdminUsers()) {
            @Override
            protected void populateItem(ListItem item) {
                item.add(new Label("lbl_admin_list_item", Model.of("Username:")));
                item.add(new Label("admin_list_item", Model.of(item.getModel().getObject().toString())));
            }
        };
        add(authorsListview);
    }
}
