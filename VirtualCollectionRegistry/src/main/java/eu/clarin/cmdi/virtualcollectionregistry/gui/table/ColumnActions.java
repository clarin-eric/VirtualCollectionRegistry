package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilteredColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnActions extends HeaderlessColumn<VirtualCollection>
        implements IFilteredColumn<VirtualCollection> {
    private final transient VirtualCollectionTable table;

    public ColumnActions(VirtualCollectionTable table) {
        super();
        this.table = table;
    }

    @Override
    public Component getFilter(String componentId, FilterForm<?> form) {
        return new AjaxGoAndClearFilter(componentId);
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.add(table.createActionPanel(componentId, model));
    }

    @Override
    public String getCssClass() {
        return "action";
    }

} // class ColumnActions
