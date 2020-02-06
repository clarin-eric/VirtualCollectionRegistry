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
package eu.clarin.cmdi.virtualcollectionregistry.gui.citation;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author wilelb
 */
public class CitationPanel extends Panel {
    
    private CitationDialog citationDialog;
    
    public CitationPanel(String id, final IModel<VirtualCollection> model) {
        super(id);
        
        AjaxLink citeButton = new AjaxLink( "citeButton", new Model<String>("Cite") ){ 
            @Override
            public void onClick( AjaxRequestTarget target ) {
                citationDialog.show(target);
            } 
        };
        UIUtils.addTooltip(citeButton, "Cite this collection");
        
        add(citeButton);
        citationDialog = new CitationDialog("citationDialog", model);
        add(citationDialog);
    }
}
