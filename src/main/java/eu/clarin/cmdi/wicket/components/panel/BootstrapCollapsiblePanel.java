/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.wicket.components.panel;

import java.util.UUID;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

/**
 *
 * @author wilelb
 */
public class BootstrapCollapsiblePanel extends AbstractBootstrapPanel {
    private final WebMarkupContainer targetCollapse;
    
    public BootstrapCollapsiblePanel(String id, String title) {
        super(id);

        String collapseid = UUID.randomUUID().toString();
        
        WebMarkupContainer btnCollapse = new WebMarkupContainer("pnl-btn-collapse");
        btnCollapse.add(new AttributeModifier("data-bs-target", "#"+collapseid));
        //btnCollapse.add(new AttributeModifier("href", "#"+collapseid));
        btnCollapse.add(new Label("pnl-title", title));
        add(btnCollapse);        
        
        targetCollapse = new WebMarkupContainer("pnl-target-collapse");
        targetCollapse.add(new AttributeModifier("id", collapseid));
        
        add(targetCollapse);
    }
    
    public BootstrapCollapsiblePanel setBody(Component body, boolean setPanelBody) {
        if(setPanelBody) {
            body.add(new AttributeModifier("class", "panel-body")); //TODO: is this replace or append? should be append
        }
        targetCollapse.add(body);
        return this;
    }
}
