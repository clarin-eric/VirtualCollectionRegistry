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
package eu.clarin.cmdi.wicket.components.pid;

import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifieable;
import eu.clarin.cmdi.wicket.components.panel.EmptyPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class PidPanel extends Panel {
    
    private final static Logger logger = LoggerFactory.getLogger(PidPanel.class);
       
    private class PidPanelContent extends Panel {
        public PidPanelContent(String id, IModel<PersistentIdentifieable> model, String context) {
            super(id);

            final String pid = model.getObject().getIdentifier();
            final String href = model.getObject().getPidUri();
            final String title = model.getObject().getPidTitle();
            String type = "N/A";
            if( model.getObject().hasPersistentIdentifier()) {
                type = model.getObject().getPidType().getShort();
            }

            final PidInfoDialog dlg = new PidInfoDialog("info_dialog", model, context);
            AjaxLink citeButton = new AjaxLink( "type", new Model<String>(type) ){ 
                @Override
                public void onClick( AjaxRequestTarget target ) {
                    if( model.getObject().hasPersistentIdentifier()) {
                        dlg.show(target);
                    }
                } 
            };
            citeButton.add(new Label("type_lbl", new Model<>(type)));
            final ExternalLink link = new ExternalLink("link", href);
            link.add(new Label("label", pid));
            link.add(new AttributeModifier("title", title));

            this.setOutputMarkupId(true);
            this.add(link);
            this.add(dlg);
            this.add(citeButton);
        }
    }
    
    public PidPanel(String id, IModel<PersistentIdentifieable> model, String context) {
        super(id);
        
        if(!model.getObject().hasPersistentIdentifier()) {
            add(new EmptyPanel("content"));
        } else {
            add(new PidPanelContent("content", model, context));
        }
        /*
        final String pid = model.getObject().getIdentifier();
        final String href = model.getObject().getPidUri();
        final String title = model.getObject().getPidTitle();
        String type = "N/A";
        if( model.getObject().hasPersistentIdentifier()) {
            type = model.getObject().getPidType().getShort();
        }
       
        final PidInfoDialog dlg = new PidInfoDialog("info_dialog", model);
        AjaxLink citeButton = new AjaxLink( "type", new Model<String>(type) ){ 
            @Override
            public void onClick( AjaxRequestTarget target ) {
                if( model.getObject().hasPersistentIdentifier()) {
                    dlg.setMaskType( ModalWindow.MaskType.SEMI_TRANSPARENT );
                    dlg.show(target);
                }
            } 
        };
        citeButton.add(new Label("type_lbl", new Model<>(type)));
        final ExternalLink link = new ExternalLink("link", href);
        link.add(new Label("label", pid));
        link.add(new AttributeModifier("title", title));
        
        this.setOutputMarkupId(true);
        this.add(link);
        this.add(dlg);
        this.add(citeButton);
        */
    }
}
