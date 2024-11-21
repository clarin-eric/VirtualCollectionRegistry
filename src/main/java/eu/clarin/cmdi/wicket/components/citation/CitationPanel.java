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
package eu.clarin.cmdi.wicket.components.citation;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import org.apache.wicket.AttributeModifier;
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

    public CitationPanel(String id, final IModel<Citable> model) {
        this(id,  model, false);
    }
    
    public CitationPanel(String id, final IModel<Citable> model, boolean small) {
        super(id);
    
        final CitationDialog citationDialog = new CitationDialog("citationDialog", model);

        AjaxLink citeButton = new AjaxLink( "citeButton", Model.of("Cite") ){ 
            @Override
            public void onClick( AjaxRequestTarget target ) {
                citationDialog.show(true, target);
            } 
        };
        
        citeButton.add(new AttributeModifier("class", small ? "btn btn-primary btn-xs" : "btn btn-primary btn-lg"));
        
        UIUtils.addTooltip(citeButton, "Cite this collection");
        
        add(citeButton);
        add(citationDialog);
    }
}
