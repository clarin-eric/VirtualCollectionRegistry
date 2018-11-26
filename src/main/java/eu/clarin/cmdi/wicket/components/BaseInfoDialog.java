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
package eu.clarin.cmdi.wicket.components;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 *
 * @author wilelb
 */
public class BaseInfoDialog extends ModalWindow {
            
    private final class Content extends Panel {
        public Content(String id, String title,  Component body) {
            super(id);
            setOutputMarkupId(true);
            //add(new Label("lbl", new Model("content")));
            add(new Label("title", new Model(title)));
            
            add(new AjaxLink( "closeButtonTop", new Model<String>("X") ){ 
                @Override
                public void onClick( AjaxRequestTarget target ) {
                    BaseInfoDialog.this.close(target);
                } 
            });
            final AbstractLink btn = new AjaxLink( "closeButtonBottom", new Model<String>("") ){ 
                @Override
                public void onClick( AjaxRequestTarget target ) {
                    BaseInfoDialog.this.close(target);
                } 
            };
            btn.add(new Label("closeButtonBottomLbl", "Close"));
            add(btn);
            add(body);
            
        }
    } // class ModalDialogBase.Content
    
    public BaseInfoDialog(String id, String title) {
        super(id);

        if (title == null) {
            throw new NullPointerException("title == null");
        }

        setOutputMarkupId(true);
        setInitialWidth(600);
        setResizable(false);
        setUseInitialHeight(false);
        setMaskType(MaskType.SEMI_TRANSPARENT);
    }
    
    protected void buildContent(String title,  Component body) {
        setContent(new Content(this.getContentId(), title, body));
    }
    
    
    @Override
    public void show(IPartialPageRequestHandler target) {
        super.show(target);
    }
    
    @Override
    protected ResourceReference newCssResource() {
        return new CssResourceReference(BaseInfoDialog.class, "modal.css");
    }
    
    protected String getContentWicketId() {
        return "content";
    }
}
