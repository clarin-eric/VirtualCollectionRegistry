package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import java.util.List;

@SuppressWarnings("serial")
final class ColumnState extends AbstractColumn<VirtualCollection, String> {
    private final EnumChoiceRenderer<VirtualCollection.State> renderer;

    private final IModel<Boolean> showVersionsModel;

    private final class ItemCell extends Panel {
        public ItemCell(String id, IModel<VirtualCollection> model) {
            super(id);

            final VirtualCollection.State state = model.getObject().getState();
            final String label = renderer.getDisplayValue(state).toString();
            add(new Label("lbl_state", ColumnType.capitaliseFirstLetter(label)));

            final List<VirtualCollection> parents = model.getObject().getParentsAsList();
            ListView list = new ListView<VirtualCollection>("list", parents) {
                @Override
                protected void populateItem(ListItem<VirtualCollection> item) {
                    final VirtualCollection.State parentState = item.getModel().getObject().getState();
                    final String parentLabel = renderer.getDisplayValue(parentState).toString();
                    item.add(new Label("lbl_parent_state", ColumnType.capitaliseFirstLetter(parentLabel)));
                }
            };
            list.setVisible(!parents.isEmpty() && showVersionsModel.getObject());
            add(list);
        }
    }

    public static class StatePanel extends Panel {
        public StatePanel(String id, final String labelText, final String problemText) {
            super(id);

            final Label lbl = new Label("lbl", labelText);
            add(lbl);

            WebMarkupContainer icon = new WebMarkupContainer("icon");
            icon.add(new AttributeModifier("class", "fa fa-info-circle"));
            icon.add(new AttributeModifier("style", "margin-left: 5px;"));
            icon.add(new AttributeModifier("data-toggle", "tooltip"));
            icon.add(new AttributeModifier("data-placement", "bottom"));
            icon.add(new AttributeModifier("title", Model.of(problemText)));
            add(icon);


        }
    }

    ColumnState(VirtualCollectionTable table, IModel<Boolean> showVersionsModel) {
        super(new ResourceModel("column.state", "State"), "state");
        this.renderer = new EnumChoiceRenderer<VirtualCollection.State>(table);
        this.showVersionsModel = showVersionsModel;
    }

    @Override
    public void populateItem(
            Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        final VirtualCollection.State state = model.getObject().getState();
        final String label = renderer.getDisplayValue(state).toString();

        if(state == VirtualCollection.State.ERROR) {
            item.add(new StatePanel(componentId, label, model.getObject().getProblem().toString()));
        } else {
            item.add(new ItemCell(componentId, model));
        }
    }

    @Override
    public String getCssClass() {
        return "state";
    }

} // class ColumnState
