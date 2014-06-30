package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.odlabs.wiquery.core.commons.IWiQueryPlugin;
import org.odlabs.wiquery.core.commons.WiQueryResourceManager;
import org.odlabs.wiquery.core.javascript.JsStatement;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnName extends AbstractColumn<VirtualCollection> {
    private static final ResourceReference JAVASCRIPT_RESOURCE =
        new ResourceReference(ColumnName.class, "ColumnName.js");
    private final VirtualCollectionTable table;

    private final class ItemCell extends Panel implements IWiQueryPlugin {
        private final WebMarkupContainer nameColumn;

        public ItemCell(String id, IModel<VirtualCollection> model) {
            super(id);
            setRenderBodyOnly(true);

            nameColumn = new WebMarkupContainer("nameColumn");
            nameColumn.setOutputMarkupId(true);
            final VirtualCollection vc = model.getObject();
            nameColumn.add(new Label("name", vc.getName()));

            final WebMarkupContainer details =
                new WebMarkupContainer("details");
            final String desc = vc.getDescription();
            final MultiLineLabel descLabel = new MultiLineLabel("desc", desc);
            if (desc == null) {
                descLabel.setVisible(false);
            }
            details.add(descLabel);
            final Panel actionsPanel =
                table.createActionPanel("actionsPanel", model);
            details.add(actionsPanel);
            details.add(new AbstractBehavior() {

                @Override
                public void bind(Component component) {
                    component.setVisible(actionsPanel.isVisible());
                }
            });

            // move to css?
            details.add(new AttributeAppender("style",
                    new Model<String>("display:none"), ";"));
            nameColumn.add(details);
            add(nameColumn);
        }

        @Override
        public void contribute(WiQueryResourceManager manager) {
            manager.addJavaScriptResource(JAVASCRIPT_RESOURCE);
        }

        @Override
        public JsStatement statement() {
            return new JsStatement().$(nameColumn).append(".detailsToggle()");
        }

    } // class ColumnName.ItemCell

    ColumnName(VirtualCollectionTable table) {
        super(new ResourceModel("column.name", "Name"), "name");
        this.table = table;
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.add(new ItemCell(componentId, model));
    }

    @Override
    public String getCssClass() {
        return "name";
    }

} // class ColumnName
