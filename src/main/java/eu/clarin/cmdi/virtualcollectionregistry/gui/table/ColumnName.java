package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilteredPropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnName extends FilteredPropertyColumn<VirtualCollection> {

    ColumnName(VirtualCollectionTable table) {
        super(new ResourceModel("column.name", "Name"), "name",
                "name");
    }

    @Override
    public Component getFilter(String componentId, FilterForm<?> form) {
        final FilterState state =
            (FilterState) form.getStateLocator().getFilterState();
        final IModel<String> model =
            new PropertyModel<String>(state, "name");
        return new TextFilter<String>(componentId, model, form);
    }

    @Override
    public String getCssClass() {
        return "name";
    }

} // class ColumnName
