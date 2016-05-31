package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.Date;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.convert.IConverter;

@SuppressWarnings("serial")
final class ColumnCreated extends AbstractColumn<VirtualCollection> {

    private static final IConverter dateConverter = new DateConverter();

    ColumnCreated(VirtualCollectionTable table) {
        super(new ResourceModel("column.created", "Created"), "created");
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.add(new Label(componentId,
                new PropertyModel<Date>(model, "creationDate")) {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <C> IConverter<C> getConverter(Class<C> type) {
                        if (Date.class.isAssignableFrom(type)) {
                            return dateConverter;
                        }
                        return super.getConverter(type);
                    }

                });
    }

    @Override
    public String getCssClass() {
        return "created";
    }

} // class ColumnCreated
