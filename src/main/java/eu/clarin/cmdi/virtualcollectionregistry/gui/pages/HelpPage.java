/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.protocol.http.WebApplication;

/**
 *
 * @author twagoo
 */
public class HelpPage extends BasePage {

    public static final String BASE_URI = "eu.clarin.cmdi.virtualcollectionregistry.base_uri";

    public HelpPage() {
        final String baseUri = WebApplication.get().getServletContext().getInitParameter(BASE_URI);
        final String serviceBaseUri = String.format("%s/service", baseUri);

        add(new ExternalLink("restLink", serviceBaseUri)
                .add(new Label("restUrl", serviceBaseUri)));
        add(new ExternalLink("wadlLink", String.format("%s/application.wadl", serviceBaseUri)));
    }

}
