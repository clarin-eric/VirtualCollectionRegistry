package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ColumnProcessing  extends AbstractColumn<VirtualCollection, String> {
    private final static Logger logger = LoggerFactory.getLogger(ColumnProcessing.class);

    ColumnProcessing(VirtualCollectionTable table) {
        super(Model.of(""), "state");
    }

    private List<Component> items = new ArrayList<>();


    public List<Component> getItems() {
        return items;
    }

    @Override
    public void populateItem(
            Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.setOutputMarkupId(true);
        //items.clear();
        //items.add(item);
        //final Component componentToUpdate = item;

        Label spinner = new Label(componentId);
        spinner.add(new AttributeModifier("class", "lds-dual-ring"));
        spinner.setVisible(
                model.getObject().getState() == VirtualCollection.State.PUBLIC_PENDING ||
                model.getObject().getState() == VirtualCollection.State.PUBLIC_FROZEN_PENDING);
        item.add(spinner);
/*
        item.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            private boolean update_state_active = false;

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if(target != null && update_state_active) {
                    logger.info("Update column processing timer");
                    target.add(componentToUpdate);
                }
                update_state_active = (
                    model.getObject().getState() == VirtualCollection.State.PUBLIC_PENDING ||
                    model.getObject().getState() == VirtualCollection.State.PUBLIC_FROZEN_PENDING
                );
            }
        });

 */
    }

}
