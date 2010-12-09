package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnType extends AbstractColumn<VirtualCollection> {
    private final EnumChoiceRenderer<VirtualCollection.Type> renderer;

    ColumnType(VirtualCollectionTable table) {
        super(new ResourceModel("column.type", "Type"), "type");
        this.renderer = new EnumChoiceRenderer<VirtualCollection.Type>(table);
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
