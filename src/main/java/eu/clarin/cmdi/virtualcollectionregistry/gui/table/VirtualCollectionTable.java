package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.gui.border.AjaxToggleBorder;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
public abstract class VirtualCollectionTable extends Panel {

    public VirtualCollectionTable(String id, CollectionsProvider provider, final boolean showState, final boolean isAdmin) {
        super(id);
        setOutputMarkupId(true);

        // setup table provider
        List<IColumn<VirtualCollection>> columns =
            new ArrayList<IColumn<VirtualCollection>>();
        columns.add(new ColumnName(this));
        if (showState) {
            columns.add(new ColumnState(this));
        }
        columns.add(new ColumnType(this));
        columns.add(new ColumnCreated(this));
        columns.add(new ColumnCitation(this));
        columns.add(new ColumnActions(this));

        // setup table
        final DataTable<VirtualCollection> table =
            new AjaxFallbackDefaultDataTable<VirtualCollection>("table",
                columns, provider, 30);
        table.add(new AttributeAppender("class",
                new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return showState ? "private" : "public";
                    }
                }, " "));
        table.addBottomToolbar(new AjaxNavigationToolbar(table));

        // setup filter
        final AjaxToggleBorder border =
            new AjaxToggleBorder("border", new Model<String>("Filter"));
        final FilterForm form =
            new FilterForm("filterForm", provider, table, showState, isAdmin);
        border.add(form);
        add(border);
        add(table);
    }

    protected abstract Panel createActionColumn(String componentId,
            IModel<VirtualCollection> model);

    protected abstract Panel createActionPanel(String componentId,
            IModel<VirtualCollection> model);

} // class VirtualCollectionTable
