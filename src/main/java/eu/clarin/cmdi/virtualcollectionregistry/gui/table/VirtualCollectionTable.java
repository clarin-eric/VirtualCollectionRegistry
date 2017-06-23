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

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

@SuppressWarnings("serial")
public abstract class VirtualCollectionTable extends Panel {

    private static final ResourceReference JAVASCRIPT_RESOURCE =
        new PackageResourceReference(VirtualCollectionTable.class, "VirtualCollectionTable.js");
    
    public VirtualCollectionTable(String id, CollectionsProvider provider, final boolean showState, final boolean isAdmin) {
        super(id);
        setOutputMarkupId(true);

        // setup table provider
        List<IColumn<VirtualCollection, String>> columns = new ArrayList<>();
        columns.add(new ColumnName(this));
        if (showState) {
            columns.add(new ColumnState(this));
        }
        columns.add(new ColumnType(this));
        columns.add(new ColumnCreated(this));
        columns.add(new ColumnCitation(this));
        columns.add(new ColumnActions(this));

        // setup table
        final DataTable<VirtualCollection, String> table =
            new AjaxFallbackDefaultDataTable<>("table",
                columns, provider, 30);
        table.add(new AttributeAppender("class",
                new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return showState ? "private table" : "public table";
                    }
                }, " "));
        table.addBottomToolbar(new AjaxNavigationToolbar(table));

        final FilterForm form =
            new FilterForm("filterForm", provider, table, showState, isAdmin);
        add(form);
        add(table);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT_RESOURCE));
    }

    protected abstract Panel createActionColumn(String componentId,
            IModel<VirtualCollection> model);

    protected abstract Panel createActionPanel(String componentId,
            IModel<VirtualCollection> model);

} // class VirtualCollectionTable
