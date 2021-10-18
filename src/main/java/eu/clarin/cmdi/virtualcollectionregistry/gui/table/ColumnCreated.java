package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.VirtualCollectionDetailsPage;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.convert.IConverter;

@SuppressWarnings("serial")
final class ColumnCreated extends AbstractColumn<VirtualCollection, String> {

    private static final IConverter dateConverter = new DateConverter();

    private final String columnPropertyExpression;

    public static class DateLabel extends Label {
        public DateLabel(final String id, final IModel<Date> model) {
            super(id, model);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <C> IConverter<C> getConverter(Class<C> type) {
            if (Date.class.isAssignableFrom(type)) {
                return dateConverter;
            }
            return super.getConverter(type);
        }
    }

    private final class ItemCell extends Panel {
        public ItemCell(String id, IModel<VirtualCollection> model) {
            super(id);

            add(new DateLabel("lbl_name", new PropertyModel<Date>(model, columnPropertyExpression)));

            final List<VirtualCollection> parents = model.getObject().getParentsAsList();
            add(new ListView<VirtualCollection>("list", parents) {
                @Override
                protected void populateItem(ListItem<VirtualCollection> item) {
                    final VirtualCollection vc = item.getModel().getObject();
                    item.add(new DateLabel("lbl_timestamp",  new PropertyModel<Date>(Model.of(vc), columnPropertyExpression)));
                }
            });
        }
    }

    ColumnCreated(VirtualCollectionTable table, String columnResourceKey, String columnDefaultValue, String columnSortProperty, String columnPropertyExpression) {
        //super(new ResourceModel("column.created", "Created"), "created");
        super(new ResourceModel(columnResourceKey, columnDefaultValue), columnSortProperty);
        this.columnPropertyExpression = columnPropertyExpression;
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.add(new ColumnCreated.ItemCell(componentId, model));
    }

    @Override
    public String getCssClass() {
        return "created";
    }

} // class ColumnCreated
