/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.wicket.components.citation;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;


/**
 * https://doi.org/10.34733/vc-1112
 * 
 * https://doi.org/10.34733/vc-1112
 * https://doi.org/10.34733/vc-1112
 * @author wilelb
 */
public class CopyToClipboardButton extends UIUtils {
    
    /**
     * Render a copy to clipboard with button with a standard tooltip text and
     * placement (top)
     * 
     * @param id The wicket component id
     * @param copyElId The id of the element containing the content we want to
     *                 copy to the clipboard
     */
    public CopyToClipboardButton(String id, String copyElId) {
        this(id, copyElId, "Copy to Clipboard", UIUtils.TOOLTIP_TOP);
    }
    
    /**
     * Render a copy to clipboard button
     * 
     * @param id The wicket component id
     * @param copyElId The id of the element containing the content we want to
     *                 copy to the clipboard
     * @param tooltipText A tooltip text to show on hover
     * @param tooltipPlacement The placement of the tooltip, relative to this component
     */
    public CopyToClipboardButton(String id, String copyElId, String tooltipText, String tooltipPlacement) {
        super(id, Model.of("<i class=\"fa-solid fa-copy\"></i>"), tooltipText, tooltipPlacement);
        setEscapeModelStrings(false);
        this.add(AttributeModifier.append("class", "btn btn-default btn-xs"));
        this.add(AttributeModifier.replace("href", "#"));
        this.add(AttributeModifier.replace("onclick", "copyElementContent(event, '"+copyElId+"', 'btnLinkClipboard', 'left');"));
    }
    
}
