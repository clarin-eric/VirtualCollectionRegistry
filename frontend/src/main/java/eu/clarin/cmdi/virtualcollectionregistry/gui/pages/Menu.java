package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class Menu extends Panel {
    private final List<MenuItem<? extends WebPage>> menuitems =
        new ArrayList<MenuItem<? extends WebPage>>();

    public Menu(String id) {
        super(id);
        setRenderBodyOnly(true);
        add(new ListView<MenuItem<? extends WebPage>>("menuitems", menuitems) {
            @Override
            protected void populateItem(
                    final ListItem<MenuItem<? extends WebPage>> listitem) {
                final MenuItem<? extends WebPage> menuitem =
                    listitem.getModelObject();
                listitem.add(menuitem);
            }
        });
    }

    public void addMenuItem(MenuItem<? extends WebPage> item) {
        menuitems.add(item);
    }

} // class Menu
