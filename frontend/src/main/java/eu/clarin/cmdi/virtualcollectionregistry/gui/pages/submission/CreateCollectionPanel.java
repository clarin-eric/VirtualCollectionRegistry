/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author wilelb
 */
public class CreateCollectionPanel extends Panel {
    private final IModel<VirtualCollection> model;
    
    public CreateCollectionPanel(String id, IModel<VirtualCollection> model) {
        super(id);
        this.model = model;
    }
 
     @Override
    protected void onBeforeRender() {
        VirtualCollection vc = model.getObject();
        add(new Label("name", new Model(vc.getName())));
        add(new Label("type", new Model(vc.getType())));
        add(new Label("created", new Model(vc.getDateCreated())));
        add(new Label("description", new Model(vc.getDescription())));
        add(new Label("purpose", new Model(vc.getPurpose())));
        add(new Label("reproducibility", new Model(vc.getReproducibility())));
        add(new Label("reproducibility_notice", new Model(vc.getReproducibilityNotice())));
        super.onBeforeRender();
    }
    
}
