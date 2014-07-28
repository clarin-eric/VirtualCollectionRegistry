package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
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
        
        // Rerence model shared by link and label
        final PropertyModel refModel = new PropertyModel(model, "ref");
        
        // Wrapper for link model that detects handles
        final HandleLinkModel linkModel = new HandleLinkModel(refModel);
        final ExternalLink link = new ExternalLink("reference", linkModel);
        
        // Set label on link
        // TODO: get label from 'label' property if available
        link.add(new Label("referenceLabel", refModel));
        
        add(link);
    }

}
