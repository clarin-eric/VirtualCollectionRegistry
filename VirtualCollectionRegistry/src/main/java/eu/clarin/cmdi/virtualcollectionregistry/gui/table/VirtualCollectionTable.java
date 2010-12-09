package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.gui.border.AjaxToggleBorder;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
public abstract class VirtualCollectionTable extends Panel {

    public VirtualCollectionTable(String id, boolean privateMode) {
        super(id);
        setOutputMarkupId(true);

        final AjaxToggleBorder border = new AjaxToggleBorder("border",
                new Model<String>("Filter"));
        add(border);

        // setup table 
        List<IColumn<VirtualCollection>> columns =
            new ArrayList<IColumn<VirtualCollection>>();
        columns.add(new ColumnName(this));
        if (privateMode) {
            columns.add(new ColumnState(this));
        }
        columns.add(new ColumnType(this));
        columns.add(new ColumnCreated(this));
        columns.add(new ColumnActions(this));
        Provider provider = new Provider(privateMode);
        final DataTable<VirtualCollection> table =
            new AjaxFallbackDefaultDataTable<VirtualCollection>("table",
                columns, provider, 4);
        table.addBottomToolbar(new AjaxNavigationToolbar(table));
        add(table);
    }

    protected abstract Panel createActionPanel(String componentId,
            IModel<VirtualCollection> model);

} // class VirtualCollectionTable
