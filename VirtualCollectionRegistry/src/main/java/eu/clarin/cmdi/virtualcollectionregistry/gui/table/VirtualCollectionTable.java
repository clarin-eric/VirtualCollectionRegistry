package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
public abstract class VirtualCollectionTable extends Panel {
    private final DataTable<VirtualCollection> table;

    public VirtualCollectionTable(String id, boolean privateMode) {
        super(id);
        setOutputMarkupId(true);

        // create columns
        List<IColumn<VirtualCollection>> columns =
            new ArrayList<IColumn<VirtualCollection>>();
        columns.add(new ColumnName(this));
        columns.add(new ColumnType(this));
        if (privateMode) {
            columns.add(new ColumnState(this));
        }
        columns.add(new ColumnDescription(this));
        columns.add(new ColumnCreated(this));
        columns.add(new ColumnActions(this));
        Provider provider = new Provider(privateMode);
        final FilterForm<FilterState> filterform =
            new FilterForm<FilterState>("filterform", provider);
        add(filterform);

        // table
        table = new AjaxFallbackDefaultDataTable<VirtualCollection>("table",
                columns, provider, 32);
        // XXX: implement own Ajax filter toolbar
        table.addTopToolbar(new FilterToolbar(table, filterform, provider));
        table.setOutputMarkupId(true);
        filterform.add(table);
        add(filterform);
    }

    protected abstract Panel createActionPanel(String componentId,
            IModel<VirtualCollection> model);

    public DataTable<VirtualCollection> getTable() {
        return table;
    }

} // class VirtualCollectionTable
