package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.ChoiceFilter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilteredAbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnType extends FilteredAbstractColumn<VirtualCollection> {
    private static final List<VirtualCollection.Type> TYPE_VALUES =
        Arrays.asList(VirtualCollection.Type.values());
    
    private transient final VirtualCollectionTable table;
    
    ColumnType(VirtualCollectionTable table) {
        super(new StringResourceModel("column.type", table, null), "type");
        this.table = table;
    }

    @Override
    public Component getFilter(String componentId, FilterForm<?> form) {
        final FilterState state =
            (FilterState) form.getStateLocator().getFilterState();
        final IModel<VirtualCollection.Type> model =
            new PropertyModel<VirtualCollection.Type>(state, "type");
        return new ChoiceFilter<VirtualCollection.Type>(componentId, model,
                form, TYPE_VALUES,
                new EnumChoiceRenderer<VirtualCollection.Type>(table), false);
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        final VirtualCollection.Type value = model.getObject().getType();
        final String key =
            value.getDeclaringClass().getSimpleName() + "." + value.name();
        item.add(new Label(componentId,
                new StringResourceModel(key, table, null)));
    }

    @Override
    public String getCssClass() {
        return "type";
    }

} // class ColumnType
