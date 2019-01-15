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

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.wicket.components.BaseInfoDialog;
import eu.clarin.cmdi.wicket.components.DialogButton;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 *
 * @author wilelb
 */
public class Dialogs {
    
    public static eu.clarin.cmdi.wicket.components.ConfirmationDialog createConfirmationPublishCollectionDialog(String id) {//, IModel<VirtualCollection> vc, List<String> warnings) {
        eu.clarin.cmdi.wicket.components.ConfirmationDialog dlg = new eu.clarin.cmdi.wicket.components.ConfirmationDialog(id, "Publish virtual collection");
        
        dlg.addButton(new DialogButton("Close") {
            @Override
            public void handleButtonClick(AjaxRequestTarget target) {
                dlg.close(target);
            }
        });
        /*
        StringBuilder sb = new StringBuilder();
        for (String warning : warnings) {
            sb.append(" -").append(warning).append("\n");
        }
*/
        StringResourceModel model = new StringResourceModel("collections.publishwarningsconfirm");
            //StringResourceModelMigration.of("collections.publishwarningsconfirm", vc, new Object[]{sb});
        
        
        dlg.setContentPanel(new PublishConfirmationDialogPanel(BaseInfoDialog.CONTENT_ID, model));
        dlg.build();
        return dlg;
    }

}
