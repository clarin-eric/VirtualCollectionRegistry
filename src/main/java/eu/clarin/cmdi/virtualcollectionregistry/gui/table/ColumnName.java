package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

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
import org.odlabs.wiquery.core.effects.EffectSpeed;
import org.odlabs.wiquery.core.effects.sliding.SlideToggle;
import org.odlabs.wiquery.core.events.Event;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryEventBehavior;
import org.odlabs.wiquery.core.javascript.JsScope;
import org.odlabs.wiquery.core.javascript.JsStatement;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class ColumnName extends AbstractColumn<VirtualCollection> {
    private final class ItemCell extends Panel {
        private final static String DETAILS_SHOWN_CLASS = "detailsShown";

        public ItemCell(String id, IModel<VirtualCollection> model) {
            super(id);
            setRenderBodyOnly(true);
            final VirtualCollection vc = model.getObject();
            final Label nameLabel = new Label("name", vc.getName());
            add(nameLabel);
            
            final WebMarkupContainer details =
                new WebMarkupContainer("details");
            details.setOutputMarkupId(true);
            
            final String desc = vc.getDescription();
            final MultiLineLabel descLabel = new MultiLineLabel("desc", desc);
            if (desc == null) {
                descLabel.setVisible(false);
            }
            details.add(descLabel);
            add(details);
            nameLabel.add(new WiQueryEventBehavior(new Event(MouseEvent.CLICK) {
                @Override
                public JsScope callback() {
                    final JsScope cb = JsScope.quickScope(new JsStatement()
                        .$(nameLabel).toggleClass(DETAILS_SHOWN_CLASS));
                    final SlideToggle effect = new SlideToggle(EffectSpeed.SLOW);
                    effect.setCallback(cb);
                    return JsScope.quickScope(new JsStatement()
                        .$(details).chain(effect));
                }
            }));
            details.add(new AttributeAppender("style",
                    new Model<String>("display:none"), ";"));
        }
    } // class ColumnName.ItemCell

    ColumnName(VirtualCollectionTable table) {
        super(new ResourceModel("column.name", "Name"), "name");
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
