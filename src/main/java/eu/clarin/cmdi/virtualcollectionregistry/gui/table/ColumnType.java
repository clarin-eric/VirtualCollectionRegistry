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
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnType extends FilteredAbstractColumn<VirtualCollection> {
    private static final List<VirtualCollection.Type> TYPE_VALUES =
        Arrays.asList(VirtualCollection.Type.values());
    private final EnumChoiceRenderer<VirtualCollection.Type> renderer;

    ColumnType(VirtualCollectionTable table) {
        super(new ResourceModel("column.type", "Type"), "type");
        this.renderer = new EnumChoiceRenderer<VirtualCollection.Type>(table);
    }

    @Override
    public Component getFilter(String componentId, FilterForm<?> form) {
        final FilterState state =
            (FilterState) form.getStateLocator().getFilterState();
        final IModel<VirtualCollection.Type> model =
            new PropertyModel<VirtualCollection.Type>(state, "type");
        return new ChoiceFilter<VirtualCollection.Type>(componentId, model,
                form, TYPE_VALUES, renderer, true);
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        final VirtualCollection.Type type = model.getObject().getType();
        final String label = renderer.getDisplayValue(type).toString();
        item.add(new Label(componentId, label));
    }

    @Override
    public String getCssClass() {
        return "type";
    }

} // class ColumnType
