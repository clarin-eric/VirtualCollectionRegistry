package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnState extends AbstractColumn<VirtualCollection, String> {
    private final EnumChoiceRenderer<VirtualCollection.State> renderer;

    public static class StatePanel extends Panel {
        public StatePanel(String id, final String labelText, final String problemText) {
            super(id);

            final Label lbl = new Label("lbl", labelText);
            add(lbl);

            WebMarkupContainer icon = new WebMarkupContainer("icon");
            icon.add(new AttributeModifier("class", "fa-solid fa-info-circle"));
            icon.add(new AttributeModifier("style", "margin-left: 5px;"));
            icon.add(new AttributeModifier("data-toggle", "tooltip"));
            icon.add(new AttributeModifier("data-placement", "bottom"));
            icon.add(new AttributeModifier("title", Model.of(problemText)));
            add(icon);
        }
    }

    ColumnState(VirtualCollectionTable table) {
        super(new ResourceModel("column.state", "State"), "state");
        this.renderer = new EnumChoiceRenderer<VirtualCollection.State>(table);
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
            Label lbl = new Label(componentId, label);
            item.add(lbl);
        }
    }

    @Override
    public String getCssClass() {
        return "state";
    }

} // class ColumnState
