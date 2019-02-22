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
package eu.clarin.cmdi.wicket.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class ConfirmationDialog extends BaseInfoDialog {    
    
    private final static Logger logger = LoggerFactory.getLogger(ConfirmationDialog.class);
    
    private final  List<DialogButton> buttons;
    
    private Component body;
    private final String title;
    private final Handler confirmHandler;
    
    public static interface Handler<T> extends Serializable {
        public void handle(AjaxRequestTarget target);
        public void setObject(IModel<T> object);
    }
    
    public ConfirmationDialog(String id, final String title, Handler confirmHandler) {
        super(id, title);
        this.title = title;
        this.confirmHandler = confirmHandler;
        this.buttons = new ArrayList<>();
    }
    
    public void build() {
        //Ensure we have a close button if no custom buttons are configured.
        if(buttons.isEmpty()) {
            buttons.add(new DialogButton("Close") {
                @Override
                public void handleButtonClick(AjaxRequestTarget target) {
                    ConfirmationDialog.this.close(target);
                }
            });
        }
        buildContent(title, body, buttons);
    }
    
    public void setContentPanel(Component content) {
        content.setMarkupId(CONTENT_ID);
        this.body = content;
        
    }
    
    public void addButton(DialogButton button) {
        buttons.add(button);
    }

   
    public void confirm(AjaxRequestTarget target) {
        /*
        try {
            try {
                prePublicationValidator.validate(vcModel.getObject());                    
                doPublish(vcModel.getObject().getId(), isFrozen());
            } catch (VirtualCollectionRegistryUsageException ex) {
                List<String> errors = new ArrayList<>();
                for(Validatable<String> error : ex.getValidationErrors()) {
                    errors.add(error.getValue());
                }
                confirmPublishCollectionDialog.showDialogue(target, vcModel, errors, isFrozen());
            }
        } catch (VirtualCollectionRegistryException ex) {
            logger.error("Could not publish collection {}, id {}", vcModel.getObject().getName(), vcModel.getObject().getId(), ex);
            Session.get().error(ex.getMessage());
        }
        */
        if(confirmHandler != null) {
            try {
                confirmHandler.handle(target);
            } catch(RuntimeException ex) {
                 ConfirmationDialog.this.close(target);
            }
        } else {
            logger.info("No confirmation handler set");
            target.add(this);
        }
    }
    
    public void onCancel(AjaxRequestTarget target) {}  

}
