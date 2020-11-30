package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnState extends AbstractColumn<VirtualCollection, String> {
    private final EnumChoiceRenderer<VirtualCollection.State> renderer;

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

        Label lbl = new Label(componentId, label);
        if(state == VirtualCollection.State.ERROR) {
            lbl.add(new AttributeModifier("data-toggle", "tooltip"));
            lbl.add(new AttributeModifier("data-placement", "bottom"));
            lbl.add(new AttributeModifier("title", model.getObject().getProblem()));
        }
        item.add(lbl);
    }

    @Override
    public String getCssClass() {
        return "state";
    }

} // class ColumnState
