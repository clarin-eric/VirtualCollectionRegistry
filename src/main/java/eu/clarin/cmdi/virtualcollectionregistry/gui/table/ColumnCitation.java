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
package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.gui.citation.CitationPanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.citation.EmptyCitePanel;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 *
 * @author wilelb
 */
public class ColumnCitation extends HeaderlessColumn<VirtualCollection, String> {
    ColumnCitation(VirtualCollectionTable table) {
        super();
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, final IModel<VirtualCollection> model) {
        if(model.getObject().hasPersistentIdentifier()) {
            item.add(new CitationPanel(componentId, model));
        } else {
            item.add(new EmptyCitePanel(componentId));
        }
    }

    @Override
    public String getCssClass() {
        return "cite";
    }
}
