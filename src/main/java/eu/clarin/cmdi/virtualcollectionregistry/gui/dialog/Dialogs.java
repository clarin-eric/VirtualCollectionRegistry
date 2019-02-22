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

import eu.clarin.cmdi.wicket.components.BaseInfoDialog;
import eu.clarin.cmdi.wicket.components.DialogButton;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

/**
 *
 * @author wilelb
 */
public class Dialogs {
    
    private static void addDefaultConfirmButtons(eu.clarin.cmdi.wicket.components.ConfirmationDialog dlg, eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler confirmHandler) {
        dlg.addButton(new DialogButton("Yes") {
            @Override
            public void handleButtonClick(AjaxRequestTarget target) {
                try {
                    confirmHandler.handle(target);
                } catch(RuntimeException ex) {
                    dlg.close(target);
                }
            }
        });      
        dlg.addButton(new DialogButton("No") {
            @Override
            public void handleButtonClick(AjaxRequestTarget target) {
                dlg.close(target);
            }
        });    
    }
    
    private static eu.clarin.cmdi.wicket.components.ConfirmationDialog createDialog(String title, String id, IModel<String> model, eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler confirmHandler) {
        eu.clarin.cmdi.wicket.components.ConfirmationDialog dlg = new eu.clarin.cmdi.wicket.components.ConfirmationDialog(id, title, confirmHandler);
        addDefaultConfirmButtons(dlg, confirmHandler);        
        dlg.setContentPanel(new PublishConfirmationDialogPanel(BaseInfoDialog.CONTENT_ID, model));
        dlg.build();
        return dlg;
    }
    
    public static eu.clarin.cmdi.wicket.components.ConfirmationDialog createConfirmPublishCollectionDialog(String id, IModel<String> model, eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler confirmHandler, eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler confirmFrozenHandler) {        
        eu.clarin.cmdi.wicket.components.ConfirmationDialog dlg = new eu.clarin.cmdi.wicket.components.ConfirmationDialog(id, "Publish virtual collection", confirmHandler);
        dlg.addButton(new DialogButton("Publish Frozen") {
            @Override
            public void handleButtonClick(AjaxRequestTarget target) {
                try {
                    confirmFrozenHandler.handle(target);
                } catch(RuntimeException ex) {
                    dlg.close(target);
                }
            }
        });  
        addDefaultConfirmButtons(dlg, confirmHandler);        
        dlg.setContentPanel(new PublishConfirmationDialogPanel(BaseInfoDialog.CONTENT_ID, model));
        dlg.build();
        return dlg;
    }
    
    public static eu.clarin.cmdi.wicket.components.ConfirmationDialog createConfirmPublishCollectionWithWarningsDialog(String id, IModel<String> model, eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler confirmHandler) {
        return createDialog("Publish virtual collection with warnings", id, model, confirmHandler);
    }

    public static eu.clarin.cmdi.wicket.components.ConfirmationDialog createConfirmEditCollectionDialog(String id, IModel<String> model, eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler confirmHandler) {
        return createDialog("Edit virtual collection", id, model, confirmHandler);
    }
    
    public static eu.clarin.cmdi.wicket.components.ConfirmationDialog createDeleteEditCollectionDialog(String id, IModel<String> model, eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler confirmHandler) {
        return createDialog("Delete virtual collection", id, model, confirmHandler);
    }
}
