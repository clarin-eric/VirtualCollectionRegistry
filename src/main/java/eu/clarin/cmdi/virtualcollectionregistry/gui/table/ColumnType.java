package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import java.util.List;

@SuppressWarnings("serial")
final class ColumnType extends AbstractColumn<VirtualCollection, String> {
    private final EnumChoiceRenderer<VirtualCollection.Type> renderer;

    public final static String capitaliseFirstLetter(String input) {
        if(input == null) {
            return null;
        }
        if(input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private final class ItemCell extends Panel {
        public ItemCell(String id, IModel<VirtualCollection> model) {
            super(id);

            final VirtualCollection.Type type = model.getObject().getType();
            final String label = renderer.getDisplayValue(type).toString();
            add(new Label("lbl_type", capitaliseFirstLetter(label)));

            final List<VirtualCollection> parents = model.getObject().getParentsAsList();
            add(new ListView<VirtualCollection>("list", parents) {
                @Override
                protected void populateItem(ListItem<VirtualCollection> item) {
                    final VirtualCollection.Type parentType = item.getModel().getObject().getType();
                    final String parentLabel = renderer.getDisplayValue(parentType).toString();
                    item.add(new Label("lbl_parent_type", capitaliseFirstLetter(parentLabel)));
                }
            });
        }
    }

    ColumnType(VirtualCollectionTable table) {
        super(new ResourceModel("column.type", "Type"), "type");
        this.renderer = new EnumChoiceRenderer<VirtualCollection.Type>(table);
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.add(new ItemCell(componentId, model));
    }

    @Override
    public String getCssClass() {
        return "type";
    }

} // class ColumnType
