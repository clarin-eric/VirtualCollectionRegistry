package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnCreated extends AbstractColumn<VirtualCollection> {
    private static final FastDateFormat df =
        FastDateFormat.getInstance("yyyy-MM-dd");

    ColumnCreated(VirtualCollectionTable table) {
        super(new ResourceModel("column.created", "Created"), "created");
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.add(new Label(componentId,
                df.format(model.getObject().getCreationDate())));
    }

    @Override
    public String getCssClass() {
        return "created";
    }

} // class ColumnCreated
