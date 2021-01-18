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

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ModalDialogBase;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author wilelb
 */
public class CitationDialog extends ModalDialogBase {
    
    private final class ButtonBar extends Panel {
        public ButtonBar(String id) {
            super(id);
            final Form<Void> form = new Form<Void>("buttonsForm");
            final AjaxButton closeButton = new AjaxButton("closeButton", new Model<String>("Close"), form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    CitationDialog.this.close(target);
                }
            };
            closeButton.setDefaultFormProcessing(false);
            form.add(closeButton);           
            add(form);
        }
    }
        
    private final class Content extends Panel {
        private Content(String id) {
            super(id);
            add(new Label("citation-caption", "Cite by persistent identifier:"));
            link  = new ExternalLink("citation-link", "#");
            linkLabel = new Label("citation-link-text", "label"); 
            link.add(linkLabel);
            add(link);         
        }
    }
    
    ExternalLink link;
    Label linkLabel;
            
    public CitationDialog(String id, final IModel<VirtualCollection> model) {
        super(id, new Model<>("Citation options for " + model.getObject().getName()));
        this.setCssClassName("/css/custom_modal.css");
        //Update models in content panel
        if(model.getObject() != null && model.getObject().hasPersistentIdentifier()) {
            PersistentIdentifier pid = model.getObject().getPrimaryIdentifier();
            link.setDefaultModel(new Model<>(pid.getActionableURI()));
            linkLabel.setDefaultModel(new Model<>(pid.getURI()));
        }
        //TODO: handle case where no PID is available yet (private collections)
    }
    
     @Override
    protected Panel createButtonBar(String id) {
        return new CitationDialog.ButtonBar(id);
    }

    @Override
    protected Panel createContent(String id) {
        CitationDialog.Content content = new CitationDialog.Content(id);
        content.add(new AttributeAppender("class", getCssClass(), " "));
        return content;
    }

    protected Model<String> getCssClass() {
        return Model.of("citationDialog");
    }
    
    @Override
    public void show(IPartialPageRequestHandler target) {
        super.show(target);
    }
}
