package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 *
 * @author twagoo
 */
public class ReferenceLinkPanel extends Panel {

    public ReferenceLinkPanel(String id, IModel<Resource> model) {
        super(id, model);
        final ExternalLink link = new ExternalLink("reference", new PropertyModel(model, "ref"));
        // TODO: get label from 'label' property if available
        link.add(new Label("referenceLabel", new PropertyModel(model, "ref")));
        add(link);
    }

}
