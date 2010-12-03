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
final class ColumnState extends FilteredAbstractColumn<VirtualCollection> {
    private final static List<VirtualCollection.State> STATE_VALUES =
        Arrays.asList(VirtualCollection.State.PRIVATE,
                      VirtualCollection.State.PUBLIC,
                      VirtualCollection.State.PUBLIC_PENDING,
                      VirtualCollection.State.DELETED);
    private transient final VirtualCollectionTable table;

    ColumnState(VirtualCollectionTable table) {
        super(new StringResourceModel("column.state", table, null), "state");
        this.table = table;
    }

    @Override
    public Component getFilter(String componentId, FilterForm<?> form) {
        final FilterState state =
            (FilterState) form.getStateLocator().getFilterState();
        final IModel<VirtualCollection.State> model =
            new PropertyModel<VirtualCollection.State>(state, "state");
        return new ChoiceFilter<VirtualCollection.State>(componentId, model,
                form, STATE_VALUES, 
                new EnumChoiceRenderer<VirtualCollection.State>(table), false);
    }

    @Override
    public void populateItem(
            Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        final VirtualCollection.State value = model.getObject().getState();
        final String key =
            value.getDeclaringClass().getSimpleName() + "." + value.name();
        item.add(new Label(componentId,
                new StringResourceModel(key, table, null)));
    }

    @Override
    public String getCssClass() {
        return "state";
    }

} // class ColumnState
