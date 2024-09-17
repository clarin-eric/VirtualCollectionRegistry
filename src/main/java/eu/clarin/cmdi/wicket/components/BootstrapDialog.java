/*
 * Copyright (C) 2024 CLARIN
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

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

/**
 *
 * @author wilelb
 */
public class BootstrapDialog<T> extends Modal<T> {
    
    public static final String CONTENT_PANEL_ID = "dlg-content-body";
    
    public BootstrapDialog(String markupId) {
        super(markupId);
    }
    
    public BootstrapDialog(String markupId, IModel<T> model) {
        super(markupId, model);
    }
    
    protected void buildContent(Component body) {
        add(body);
    }
    
    public void show(boolean visible, AjaxRequestTarget target) {
        super.show(visible);
        target.add(this);
    }
}
