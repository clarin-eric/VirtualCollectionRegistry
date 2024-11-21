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

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class PublishConfirmationDialogPanel extends Panel {
    private final static  Logger logger = LoggerFactory.getLogger(PublishConfirmationDialogPanel.class);
    
    public PublishConfirmationDialogPanel(String id, IModel<String> msgModel, IModel<Boolean> immutableModel) {
        super(id);
        
        MultiLineLabel messageLabel = new MultiLineLabel("message", msgModel);
        add(messageLabel);
        
        
        CheckBox cb = new CheckBox("cb_immutable", immutableModel);
        UIUtils.addTooltip(cb, "Publish this collection as immutable. This means the collection cannot be changed after publishing.");
        
        cb.add(new AjaxFormComponentUpdatingBehavior("click") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                logger.trace("CB changed on click: {}", immutableModel.getObject());
            }
        });
        
        cb.add(new AjaxFormComponentUpdatingBehavior("blur") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                logger.trace("CB changed on blur: {}", immutableModel.getObject());
            }
        });
        
        
        add(cb);
        add(new Label("lbl_immutable", "Immutable"));
    }
}
