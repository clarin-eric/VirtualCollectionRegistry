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

import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class BaseInfoDialog extends ModalWindow {
            
    private final static Logger logger = LoggerFactory.getLogger(BaseInfoDialog.class);
    
    public static final String CONTENT_ID = "dlg-content-body";
    
    private final class Content extends Panel {
        public Content(String id, String title, Component body, List<DialogButton> buttons) {
            super(id);

            add(new Label("title", new Model(title)));            
            add(new AjaxLink( "closeButtonTop", new Model<String>("X") ){ 
                @Override
                public void onClick( AjaxRequestTarget target ) {
                    BaseInfoDialog.this.close(target);
                } 
            });

            add(body);
            add(new ListView<DialogButton>("buttons", buttons) {
		@Override
		protected void populateItem(ListItem<DialogButton> item) {                    
                    final AbstractLink btn = new AjaxLink( "button", new Model<String>("") ){ 
                        @Override
                        public void onClick( AjaxRequestTarget target ) {
                            item.getModelObject().handleButtonClick(target);
                        } 
                    };
                    btn.add(new Label("btn-label", new Model(item.getModelObject().getLabel())));
                    item.add(btn);
                   item.setRenderBodyOnly(true);
		}			
            });
            
        }
    } // class ModalDialogBase.Content
       
    private Component body;
    
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
    
    protected void buildContent(String title, Component body, List<DialogButton> buttons) {
        logger.info("Body markup id = " + body.getMarkupId());
        setContent(new Content(this.getContentId(), title, body, buttons));
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
        return CONTENT_ID;
    }
}
