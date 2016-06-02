/*
 * Copyright (C) 2016 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.gui.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 *
 * @author wilelb
 */
public class AjaxPopupMenu2 extends Panel implements Serializable {
    
    private static final ResourceReference CSS_RESOURCE =
        new CssResourceReference(AjaxPopupMenu2.class, "AjaxPopupMenu.css");
    
    private static final ResourceReference JAVASCRIPT_RESOURCE =
        new PackageResourceReference(AjaxPopupMenu2.class, "AjaxPopupMenu2.js");
    
    private final List<MenuItem> items = new ArrayList<>();

    public AjaxPopupMenu2(String id, IModel<String> title) {
        super(id);
        setRenderBodyOnly(true);

        add(new Label("title", title));
        add(new ListView<MenuItem>("items", items) {
            @Override
            protected void populateItem(ListItem<MenuItem> item) {
                final MenuItem menuitem = item.getModelObject();
                final AbstractLink link = menuitem.newLink("link");
                link.add(new Label("label", menuitem.getLabel()));
                String cssClass = menuitem.getCssClass();
                if (cssClass != null) {
                    link.add(new AttributeModifier("class", cssClass));
                }
                item.add(link);
                item.setVisible(menuitem.isVisible());
                item.setEnabled(menuitem.isEnabled());
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(CSS_RESOURCE));
        response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT_RESOURCE));
    }
    
    public void add(MenuItem item) {
        items.add(item);
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
}
