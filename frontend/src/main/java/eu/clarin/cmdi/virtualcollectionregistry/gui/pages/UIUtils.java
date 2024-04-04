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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class UIUtils {
    
    private static Logger logger = LoggerFactory.getLogger(UIUtils.class);

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
}
