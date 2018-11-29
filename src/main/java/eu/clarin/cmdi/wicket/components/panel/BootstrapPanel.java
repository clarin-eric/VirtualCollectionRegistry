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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

/**
 *
 * @author wilelb
 */
public class BootstrapPanel extends AbstractBootstrapPanel {
    public BootstrapPanel(String id, String title) {
        super(id);
        add(new Label("pnl-title", title));
    }
    
    public BootstrapPanel setBody(Component body, boolean setPanelBody) {
        if(setPanelBody) {
            body.add(new AttributeModifier("class", "panel-body"));
        }
        add(body);
        return this;
    }
    
}
