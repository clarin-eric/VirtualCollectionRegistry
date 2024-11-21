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

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import java.io.Serializable;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class ConfirmationDialog extends BootstrapDialog {
    
    private final static Logger logger = LoggerFactory.getLogger(ConfirmationDialog.class);
    
    private CheckBox cb;

    private final Handler confirmHandler;
    private boolean useButtonDefault = true;
    
    public static interface Handler<T> extends Serializable {
        public void handle(AjaxRequestTarget target);
        public void setObject(IModel<T> object);
    }
    
     public static interface PublishHandler<T> extends Handler<T>, Serializable {
        //public void handle(AjaxRequestTarget target);
        //public void setObject(IModel<T> object);
        public void setFrozen(boolean frozen);
    }
    
    public ConfirmationDialog(String id, final String title, Handler confirmHandler) {
        super(id, Model.of(title));
        this.confirmHandler = confirmHandler;
        header(Model.of(title));
    }
    
    public void build() {
        //Ensure we have a close button if no custom buttons are configured.
        if(useButtonDefault) {
            addButton(new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Model.of(""), Buttons.Type.Primary, Model.of("Close")) {               
                @Override
                public void onClick(AjaxRequestTarget target) {
                    ConfirmationDialog.this.close(target); 
                }
            });
        }
    }

    @Override
    public Modal addButton(Component c) {
        this.useButtonDefault = false;
        return super.addButton(c);        
    }
    
    public void addCheckbox(CheckBox cb) {
        this.cb = cb;
    }
}
