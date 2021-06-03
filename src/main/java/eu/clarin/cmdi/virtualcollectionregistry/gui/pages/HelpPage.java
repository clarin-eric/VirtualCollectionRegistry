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
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.ContextRelativeResource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author twagoo
 */
public class HelpPage extends BasePage {

    public static final String BASE_URI = "eu.clarin.cmdi.virtualcollectionregistry.base_uri";

    private static Logger logger = LoggerFactory.getLogger(HelpPage.class);

    public HelpPage() {
        String baseUri = WebApplication.get().getServletContext().getInitParameter(BASE_URI);
        String contextPath = WebApplication.get().getServletContext().getContextPath();
        if(baseUri.endsWith("/")) {
            baseUri = baseUri.substring(0, baseUri.length()-1);
        }
        baseUri += contextPath;
        if(baseUri.endsWith("/")) {
            baseUri = baseUri.substring(0, baseUri.length()-1);
        }

        final String serviceBaseUri = String.format("%s/service/", baseUri);
        add(new ExternalLink("restLink", serviceBaseUri)
                .add(new Label("restUrl", serviceBaseUri)));
        add(new ExternalLink("restLink2", serviceBaseUri));

        final String oaiIdentifyUri = String.format("%s/oai?verb=Identify", baseUri);
        add(new ExternalLink("oaiLink", oaiIdentifyUri)
                .add(new Label("oaiUrl", oaiIdentifyUri)));

        String wadlUri = serviceBaseUri;
        if(!serviceBaseUri.endsWith("/")) {
            wadlUri += "/";
        }
        wadlUri += "application.wadl";
        add(new ExternalLink("wadlLink", wadlUri));

        add(new Image("img-virtualcollection", new ContextRelativeResource("/images/virtualcollection.png")));
        add(new Image("img-vcr-menu-browse", new ContextRelativeResource("/images/help-vcr-menu-browse.png")));
        add(new Image("img-vcr-filter", new ContextRelativeResource("/images/help-vcr-filter.png")));
        add(new Image("img-vcr-citation-dialog", new ContextRelativeResource("/images/help-vcr-citation-dialog.png")));
        add(new Image("img-vcr-menu-create", new ContextRelativeResource("/images/help-vcr-menu-create.png")));
        add(new Image("img-vcr-editor-modes", new ContextRelativeResource("/images/help-vcr-editor-modes.png")));
        add(new Image("img-vcr-menu-my-collections", new ContextRelativeResource("/images/help-vcr-menu-my-collections.png")));
        add(new Image("img-vcr-lifecycle", new ContextRelativeResource("/images/vcr-lifecycle.png")));
    }

}
