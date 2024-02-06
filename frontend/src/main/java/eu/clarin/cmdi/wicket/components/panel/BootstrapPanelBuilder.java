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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author wilelb
 */
public class BootstrapPanelBuilder {
    
    public static BootstrapPanelBuilder createCollapsiblePanel(String id ){
        return new BootstrapPanelBuilder(id, Type.COLLAPSIBLE);
    }
    
    public static BootstrapPanelBuilder createPanel(String id ){
        return new BootstrapPanelBuilder(id, Type.NORMAL);
    }
    
    private final String id;
    private final Type type;
    private String title;
    private Component body;
    private boolean setPanelBody;
    private boolean visible;
    
    public enum Type {
        NORMAL, COLLAPSIBLE
    }
    
    private BootstrapPanelBuilder(String id, Type type) {
        this.id = id;
        this.type = type;
        this.visible = true;
        this.setPanelBody = true;
        this.title = "Title not set";
        this.body = null;
    }
    
    public BootstrapPanelBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public BootstrapPanelBuilder setBody(Component body) {
        return setBody(body, true);
    }
    
    public BootstrapPanelBuilder setBody(Component body, boolean setPanelBody) {
        this.body = body;
        this.setPanelBody = setPanelBody;
        return this;
    }
    
    public BootstrapPanelBuilder setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }
    
    public Panel build() {
        Panel p = null;
        
        switch(type) {
            case NORMAL: 
                p = new BootstrapPanel(id, title).setBody(body, setPanelBody); 
                break;
            case COLLAPSIBLE: 
                p = new BootstrapCollapsiblePanel(id, title).setBody(body, setPanelBody); 
                break;
        }
        
        if(p == null) {
            throw new IllegalStateException("Body panel cannot be null");
        }
        
        p.setVisible(visible);
        return p;
    }
    
}
