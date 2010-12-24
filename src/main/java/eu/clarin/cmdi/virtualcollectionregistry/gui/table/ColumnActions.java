package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnActions extends HeaderlessColumn<VirtualCollection> {
    private final VirtualCollectionTable table;

    public ColumnActions(VirtualCollectionTable table) {
        super();
        this.table = table;
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.add(table.createActionColumn(componentId, model));
    }

    @Override
    public String getCssClass() {
        return "action";
    }

} // class ColumnActions
