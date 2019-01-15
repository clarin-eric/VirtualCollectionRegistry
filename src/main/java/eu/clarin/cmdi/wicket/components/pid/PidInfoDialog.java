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

import eu.clarin.cmdi.wicket.components.BaseInfoDialog;
import eu.clarin.cmdi.wicket.components.DialogButton;
import java.util.Arrays;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author wilelb
 */
public class PidInfoDialog extends BaseInfoDialog {
 
    private final static String TITLE = "Persistent Identifier information";
    
    private final IModel<PersistentIdentifieable> model;
    
    public PidInfoDialog(String id, final IModel<PersistentIdentifieable> model) {
        super(id, TITLE);
        this.model = model;
        this.build();
    }
    
    private void build() {
         List<DialogButton> buttons = Arrays.asList(
                new DialogButton("Close") {
                    @Override
                    public void handleButtonClick(AjaxRequestTarget target) {
                        PidInfoDialog.this.close(target);
                    }
                });
        buildContent(TITLE, new Body(getContentWicketId()), buttons);
    }
    
    private class Body extends Panel {
        public Body(String id) {
            super(id);
            TextField<String> input = new TextField("pid", new Model(model.getObject().getPidUri()));
            add(input);
        }
    }
}
