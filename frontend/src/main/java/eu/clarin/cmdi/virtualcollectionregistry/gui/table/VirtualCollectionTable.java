package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.TimerManager;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import org.apache.wicket.PageReference;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class VirtualCollectionTable extends Panel {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionTable.class);

    private static final ResourceReference JAVASCRIPT_RESOURCE =
        new PackageResourceReference(VirtualCollectionTable.class, "VirtualCollectionTable.js");

    protected Model<Boolean> toggleShowVersionsModel = Model.of(false);

    private TimerManager timerManager;

    final private  CollectionsProvider provider;
    final private DataTable<VirtualCollection, String> table;
    final private ColumnProcessing processing;
    final private List<VirtualCollection.State> publishing_states = new ArrayList<>();

    public VirtualCollectionTable(String id, CollectionsProvider provider, final boolean showState, final boolean isAdmin, TimerManager timerManager) {
        super(id);
        this.timerManager = timerManager;
        this.provider = provider;
        setOutputMarkupId(true);
        final Component componentToUpdate = this;

        publishing_states.add(VirtualCollection.State.PUBLIC_PENDING);
        publishing_states.add(VirtualCollection.State.PUBLIC_FROZEN_PENDING);

        // setup table provider
        List<IColumn<VirtualCollection, String>> columns = new ArrayList<>();
        columns.add(new ColumnName(this, toggleShowVersionsModel));
        if (showState) {
            columns.add(new ColumnState(this, toggleShowVersionsModel));
            processing = new ColumnProcessing(this);
            columns.add(processing);
        } else {
            processing = null;
        }
        columns.add(new ColumnType(this, toggleShowVersionsModel));
        columns.add(new ColumnCreated(this, "column.created", "Created", "created", "dateCreated", toggleShowVersionsModel));
        columns.add(new ColumnCreated(this, "column.modified", "Modified", "modified", "dateModified", toggleShowVersionsModel));
        columns.add(new ColumnActions(this));

        // setup table
        table =
            new AjaxFallbackDefaultDataTable<>("table",
                columns, provider, 30);
        
        table.add(new AttributeAppender("class",
                new AbstractReadOnlyModel<String>() {
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

        //Toggle help checkbox
        WebMarkupContainer cbShowVersions = new WebMarkupContainer("btn_toggle_show_versions_lbl", Model.of("Show collection versions"));
        AjaxCheckBox cb = new AjaxCheckBox("btn_toggle_show_versions", toggleShowVersionsModel) {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                toggleShowVersionsModel.getObject();
                if (target != null) {
                    target.add(componentToUpdate);
                }
            }
        };
        cbShowVersions.add(cb);
        add(cbShowVersions);
/*
        final List<VirtualCollection.State> publishing_states = new ArrayList<>();
        publishing_states.add(VirtualCollection.State.PUBLIC_PENDING);
        publishing_states.add(VirtualCollection.State.PUBLIC_FROZEN_PENDING);
        timerManager.addTarget(new TimerManager.Update() {
            @Override
            public boolean onUpdate() {
                return provider.countCollectionsWithStates(publishing_states) > 0;
            }

            @Override
            public List<Component> getComponent() {
                return table;
            }
        });
*/
        /*
        add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            private boolean update_state_active = false;

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if(target != null && update_state_active) {
                    logger.info("Update virtual collection table timer");
                    target.add(table);
                }
                update_state_active = provider.countCollectionsWithStates(publishing_states) > 0;
            }
        });

         */
    }

    public void triggerUpdateTimer(AjaxRequestTarget target) {
        timerManager.addTarget(target, new TimerManager.Update() {
            @Override
            public boolean onUpdate(AjaxRequestTarget target) {
                return provider.countCollectionsWithStates(publishing_states) > 0;
            }

            @Override
            public List<Component> getComponents() {
                List<Component> result = new ArrayList<>();
                result.add(table);
                for(Component c : processing.getItems()) {
                    result.add(c);
                }
                return result;
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
/*
    protected abstract Panel createActionPanel(String componentId,
            IModel<VirtualCollection> model);
*/
    protected abstract PageReference getPageReference();
} // class VirtualCollectionTable
