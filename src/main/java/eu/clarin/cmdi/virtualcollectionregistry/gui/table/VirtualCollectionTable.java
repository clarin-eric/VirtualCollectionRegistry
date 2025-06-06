package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.time.Duration;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class VirtualCollectionTable extends Panel {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionTable.class);

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
            columns.add(new ColumnProcessing(this));
        }
        columns.add(new ColumnType(this));
        columns.add(new ColumnCreated(this));
        columns.add(new ColumnActions(this));

        // setup table
        final DataTable<VirtualCollection, String> table =
            new AjaxFallbackDefaultDataTable<>("table",
                columns, provider, 30);
        
        table.add(new AttributeAppender("class",
                new IModel<String>() {
                    @Override
                    public String getObject() {
                        return showState ? "private table" : "public table";
                    }
                }, " ")) ;
        table.addBottomToolbar(new AjaxNavigationToolbar(table));

        final FilterForm form =
            new FilterForm("filterForm", provider, provider.getOrigins(), table, showState, isAdmin);
        add(form);
        add(table);

        //Use a timer to refresh the ui to make sure it is updated while the collections are being processed
        add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(5)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if(target != null) {
                    target.add(table);
                }
            }
        });

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

    protected abstract PageReference getPageReference();
} // class VirtualCollectionTable
