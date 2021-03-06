/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.LoginPage;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 *
 * @author wilelb
 */
public class LoginPanel extends Panel {

    public LoginPanel(String id) {
        super(id);       
    }
    
    @Override
    public void onBeforeRender() {
         add(new Label("label", "Not logged in"));
        Link submitButton = new Link("btn-login", new Model("Login now to continue")) {
            @Override
            public void onClick() {
                throw new RestartResponseException(LoginPage.class);
            }
        };
        submitButton.add(new Label("btn_label", "Click here to login"));
        add(submitButton);
        super.onBeforeRender();
    }
}
