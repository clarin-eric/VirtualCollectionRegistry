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
package eu.clarin.cmdi.virtualcollectionregistry.gui.dialog;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import eu.clarin.cmdi.wicket.components.BootstrapDialog;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.wicket.components.ConfirmationDialog;
import org.apache.wicket.markup.ComponentTag;

/**
 *
 * @author wilelb
 */
public class Dialogs {

    private final static Logger logger = LoggerFactory.getLogger(Dialogs.class);

    private abstract static class PrimaryBootstrapAjaxLink extends BootstrapAjaxLink {
        public PrimaryBootstrapAjaxLink(IModel<String> labelModel) {
            super(Modal.BUTTON_MARKUP_ID, Model.of(""), Buttons.Type.Primary, labelModel);
        }
    }
    
    private static BootstrapAjaxLink createBootstrapAjaxLink(Buttons.Type type, IModel<String> labelModel, Modal dlg, ConfirmationDialog.Handler handler) {
        return new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Model.of(""), type, labelModel) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if(handler != null) {
                    try {
                        handler.handle(target);
                    } catch(RuntimeException ex) {
                        dlg.close(target);
                    }
                } else {
                    dlg.close(target);
                }
            }            
            
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                //Attributes.addClass(tag, " btn-default");
            }
        };
    }
    
    private static ConfirmationDialog createDialog(String title, String id, IModel<String> model, ConfirmationDialog.Handler confirmHandler) {
        ConfirmationDialog dlg = new ConfirmationDialog(id, title, confirmHandler);
        dlg.addButton(createBootstrapAjaxLink(Buttons.Type.Primary, Model.of("Yes"), dlg, confirmHandler));
        dlg.addButton(createBootstrapAjaxLink(Buttons.Type.Primary, Model.of("No"), dlg, null)); 
        dlg.add(new ConfirmationDialogPanel(BootstrapDialog.CONTENT_PANEL_ID, model));
        dlg.build();
        return dlg;
    }
    
    public static ConfirmationDialog createConfirmPublishCollectionWithWarningsDialog(String id, IModel<String> model, ConfirmationDialog.Handler confirmHandler) {
        return createDialog("Publish virtual collection with warnings", id, model, confirmHandler);
    }

    public static ConfirmationDialog createConfirmEditCollectionDialog(String id, IModel<String> model, ConfirmationDialog.Handler confirmHandler) {
        return createDialog("Edit virtual collection", id, model, confirmHandler);
    }
    
    public static ConfirmationDialog createDeleteEditCollectionDialog(String id, IModel<String> model, ConfirmationDialog.Handler confirmHandler) {
        return createDialog("Delete virtual collection", id, model, confirmHandler);
    }
    
    public static ConfirmationDialog createConfirmPublishCollectionDialog(String id, IModel<String> model, ConfirmationDialog.Handler confirmHandler, eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler confirmFrozenHandler) {        
        final ConfirmationDialog dlg = new ConfirmationDialog(id, "Publish virtual collection", confirmHandler);
        final IModel<Boolean> immutableModel = new Model<>(false);
        dlg.addButton(new PrimaryBootstrapAjaxLink(Model.of("Publish")) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                logger.debug("Immutable: ", immutableModel.getObject());
                try {
                    if(immutableModel.getObject()) {
                        confirmFrozenHandler.handle(target);
                    } else {
                        confirmHandler.handle(target);
                    }
                } catch(RuntimeException ex) {
                    dlg.close(target);
                }
                immutableModel.setObject(false);
            }
        });
        dlg.addButton(createBootstrapAjaxLink(Buttons.Type.Primary, Model.of("Cancel"), dlg, null));
        dlg.add(new PublishConfirmationDialogPanel(BootstrapDialog.CONTENT_PANEL_ID, model, immutableModel));
        dlg.build();
        return dlg;
    }
}
