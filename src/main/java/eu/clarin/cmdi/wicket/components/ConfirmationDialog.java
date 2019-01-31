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

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author wilelb
 */
public class ConfirmationDialog extends BaseInfoDialog {    
    
    private final  List<DialogButton> buttons;
    
    private Component body;
    private final String title;
    
    public ConfirmationDialog(String id, final String title) {
        super(id, title);
        this.title = title;
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
}
