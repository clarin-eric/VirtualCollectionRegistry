package eu.clarin.cmdi.virtualcollectionregistry.gui.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.odlabs.wiquery.core.commons.IWiQueryPlugin;
import org.odlabs.wiquery.core.commons.WiQueryResourceManager;
import org.odlabs.wiquery.core.javascript.JsStatement;

@SuppressWarnings("serial")
public class AjaxPopupMenu extends Panel implements Serializable, IWiQueryPlugin {
    private static final ResourceReference CSS_RESOURCE =
        new ResourceReference(AjaxPopupMenu.class, "AjaxPopupMenu.css");
    private static final ResourceReference JAVASCRIPT_RESOURCE =
        new ResourceReference(AjaxPopupMenu.class, "AjaxPopupMenu.js");
    private final WebMarkupContainer menu;
    private List<MenuItem> items = new ArrayList<MenuItem>();

    public AjaxPopupMenu(String id, IModel<String> title) {
        super(id);
        setRenderBodyOnly(true);

        menu = new WebMarkupContainer("menu");
        menu.setOutputMarkupId(true);
        menu.add(new Label("title", title));
        menu.add(new ListView<MenuItem>("items", items) {
            @Override
            protected void populateItem(ListItem<MenuItem> item) {
                final MenuItem menuitem = item.getModelObject();
                final AbstractLink link = menuitem.newLink("link");
                link.add(new Label("label", menuitem.getLabel()));
                String cssClass = menuitem.getCssClass();
                if (cssClass != null) {
                    link.add(new SimpleAttributeModifier("class", cssClass));
                }
                item.add(link);
                item.setVisible(menuitem.isVisible());
                item.setEnabled(menuitem.isEnabled());
            }
        });
        add(menu);
    }

    public void add(MenuItem item) {
        items.add(item);
    }

    @Override
    public void contribute(WiQueryResourceManager manager) {
        manager.addJavaScriptResource(JAVASCRIPT_RESOURCE);
        manager.addCssResource(CSS_RESOURCE);
    }

    @Override
    public JsStatement statement() {
        return new JsStatement().$(menu).append(".ajaxPopupMenu()");
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        boolean anyVisible = false;
        for (MenuItem item : items) {
            if (item.isVisible()) {
                anyVisible = true;
                break;
            }
        }
        if (!anyVisible) {
            setVisible(false);
        }
    }

} // class PopupMenu
