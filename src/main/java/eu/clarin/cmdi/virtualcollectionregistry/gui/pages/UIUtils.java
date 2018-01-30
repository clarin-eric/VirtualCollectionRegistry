/*
 * Copyright (C) 2017 CLARIN
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

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class UIUtils {
    
    private static Logger logger = LoggerFactory.getLogger(UIUtils.class);
    
    public static Component addTooltip(Component comp, String tooltipText) {
        comp.add(new AttributeAppender("data-toggle", Model.of("tooltip")));
        comp.add(new AttributeAppender("data-placement", Model.of(CreateAndEditVirtualCollectionPage.DEFAULT_TOOLTIP_DATA_PLACEMENT)));
        comp.add(new AttributeAppender("data-html", Model.of("true")));
        comp.add(new AttributeAppender("data-trigger", Model.of("hover")));
        comp.add(new AttributeAppender("title", Model.of(tooltipText)));
        return comp;
    }
    
            
    public static AjaxLink getLrsRedirectAjaxLink(String id, IModel<VirtualCollection> model) {
        final AjaxLink<VirtualCollection> lrsLink
            = new AjaxLink<VirtualCollection>(id, model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    throw new RedirectToUrlException(UIUtils.getLanguageSwitchboardUrl(model.getObject()));
                }
        };
        UIUtils.addTooltip(lrsLink, "Open this collection in the language resurce switchboard");
        return lrsLink;
    }
    
    
    public static String getLanguageSwitchboardUrl(VirtualCollection vc) {
        try {
            //create link for this resource to the language resource switchboard
            final String href = "http://localhost:8080/vcr/service/virtualcollections/"+vc.getId();
            final String mimeType =  "application/xml";
            final String languageCode = "en";
            return String.format("%s#/vlo/%s/%s/%s",
                    "http://weblicht.sfs.uni-tuebingen.de/clrs/",
                    URLEncoder.encode(href, "UTF-8"),
                    URLEncoder.encode(mimeType, "UTF-8"), languageCode);
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error while creating switchboard link", ex);
            return null;
        }
    }
    
    
}
