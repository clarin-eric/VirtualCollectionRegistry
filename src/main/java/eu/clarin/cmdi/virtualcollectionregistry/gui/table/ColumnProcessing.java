package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;

public class ColumnProcessing  extends AbstractColumn<VirtualCollection, String> {
    ColumnProcessing(VirtualCollectionTable table) {
        super(Model.of(""), "state");
    }

    @Override
    public void populateItem(
            Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.setOutputMarkupId(true);
        final Component componentToUpdate = item;

        Label spinner = new Label(componentId);
        spinner.add(new AttributeModifier("class", "lds-dual-ring"));
        spinner.setVisible(
                model.getObject().getState() == VirtualCollection.State.PUBLIC_PENDING ||
                model.getObject().getState() == VirtualCollection.State.PUBLIC_FROZEN_PENDING);
        item.add(spinner);


        item.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if(target != null) {
                    target.add(componentToUpdate);
                }
            }
        });
    }

}
