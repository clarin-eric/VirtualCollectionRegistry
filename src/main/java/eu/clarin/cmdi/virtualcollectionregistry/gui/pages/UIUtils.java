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
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;

/**
 *
 * @author wilelb
 */
public class UIUtils {
    
    private static Logger logger = LoggerFactory.getLogger(UIUtils.class);
    
    private final static String TOOLTIP_COLLECTION_TEXT = "Open this collection in the language resource switchboard";
    private final static String TOOLTIP_RESOURCE_TEXT = "Open this resource in the language resource switchboard";
    
    public final static String DEFAULT_TOOLTIP_DATA_PLACEMENT = "bottom";
    
    public static Component addTooltip(Component comp, String tooltipText) {
        return addTooltip(comp, tooltipText, null, DEFAULT_TOOLTIP_DATA_PLACEMENT);
    }
    
    public static Component addTooltip(Component comp, String tooltipText, String viewport, String tooltipPlacement) {
        comp.add(new AttributeAppender("data-toggle", Model.of("tooltip")));
        comp.add(new AttributeAppender("data-placement", Model.of(tooltipPlacement)));
        comp.add(new AttributeAppender("data-html", Model.of("true")));
        comp.add(new AttributeAppender("data-trigger", Model.of("hover")));
        comp.add(new AttributeAppender("data-animation", Model.of("true")));
        comp.add(new AttributeAppender("data-delay", Model.of("500")));
        comp.add(new AttributeAppender("title", Model.of(tooltipText)));
        if(viewport != null) {
            if(!viewport.startsWith("#")) {
                viewport = "#"+viewport;
            }
            comp.add(new AttributeAppender("data-viewport", Model.of(viewport)));
            comp.add(new AttributeAppender("data-container", Model.of(viewport)));
        }
        return comp;
    }
            
    public static AjaxLink getLrsRedirectAjaxLink(String id, IModel<VirtualCollection> model, String endpoint) {
        final AjaxLink<VirtualCollection> lrsLink
            = new AjaxLink<VirtualCollection>(id, model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    throw new RedirectToUrlException(UIUtils.getLanguageSwitchboardUrl(model.getObject(), endpoint));
                }
        };
        UIUtils.addTooltip(lrsLink, TOOLTIP_COLLECTION_TEXT);
        return lrsLink;
    }
    
    public static AjaxLink getLrsRedirectAjaxLinkForResource(String id, IModel<Resource> model, String endpoint) {
        final AjaxLink<Resource> lrsLink
            = new AjaxLink<Resource>(id, model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    throw new RedirectToUrlException(UIUtils.getLanguageSwitchboardUrlForResource(model.getObject(), endpoint));
                }
        };
        UIUtils.addTooltip(lrsLink, TOOLTIP_RESOURCE_TEXT);
        return lrsLink;
    }
    
    public static String getLanguageSwitchboardUrl(VirtualCollection vc, String endpoint) {
        final String href = "http://localhost:8080/vcr/service/virtualcollections/"+vc.getId();
        return buildSwitchboardUrl(endpoint, href, "application/xml", "en");
    }
    
    public static String getLanguageSwitchboardUrlForResource(Resource r, String endpoint) {
        return buildSwitchboardUrl(endpoint, r.getRef(), "application/xml", "en");        
    }
    
    public static String buildSwitchboardUrl(String switchboardEndpoint, String href, String mimeType, String languageCode) {
        try {
            return String.format("%s/%s/%s/%s",
                    switchboardEndpoint,
                    URLEncoder.encode(href, "UTF-8"),
                    URLEncoder.encode(mimeType, "UTF-8"), 
                    languageCode);
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error while creating switchboard link", ex);
            return null;
        }
    }
}
