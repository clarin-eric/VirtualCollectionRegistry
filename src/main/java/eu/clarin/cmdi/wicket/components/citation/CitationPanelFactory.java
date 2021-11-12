/*
 * Copyright (C) 2016 CLARIN
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

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class CitationPanelFactory {

    private static Logger logger = LoggerFactory.getLogger(CitationPanelFactory.class);

    public static Panel getCitationPanel(final String componentId, final IModel<VirtualCollection> model) {
        return getCitationPanel(componentId, model, false);
    }
    /**
     * Get a citation panel or place holder if the virtual collection is not
     * citable. PUBLIC and PUBLIC_FROZEN collections with a PID are considered
     * citable.
     * 
     * @param componentId
     * @param model
     * @return 
     */
    public static Panel getCitationPanel(final String componentId, final IModel<VirtualCollection> model, boolean small) {
        VirtualCollection vc = model.getObject();
        if(vc.isCiteable()) {
            VirtualCollection latest = vc.getAllVersions().get(0);
            logger.info("current={}, latest={}", vc.getId(), latest.getId());
            if(latest.getId() != vc.getId() && latest.isCiteable()) {
                return new CitationPanel(componentId, new Model(vc), new Model(latest), small);
            }
            return new CitationPanel(componentId, new Model(vc), small);
        } else {
            return new EmptyCitePanel(componentId);
        }
    }
    
}
