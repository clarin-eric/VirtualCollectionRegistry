package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
public abstract class VirtualCollectionTable extends Panel {

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
        final FilterForm<FilterState> form =
            new FilterForm<FilterState>("filterform", provider);
        form.setOutputMarkupId(true);
        add(form);

        // table
        final DataTable<VirtualCollection> table =
            new AjaxFallbackDefaultDataTable<VirtualCollection>("table",
                columns, provider, 32);
        table.addTopToolbar(new FilterToolbar(table, form, provider));
        table.setOutputMarkupId(true);
        form.add(table);
        form.add(new FeedbackPanel("feedback"));
        add(form);
    }

    protected abstract Panel createActionPanel(String componentId,
            IModel<VirtualCollection> model);

} // class VirtualCollectionTable
