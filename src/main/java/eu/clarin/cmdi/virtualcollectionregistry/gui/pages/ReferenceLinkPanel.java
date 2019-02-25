package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.wicket.components.pid.PersistentIdentifieable;
import eu.clarin.cmdi.wicket.components.pid.PidPanel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class ReferenceLinkPanel extends Panel {

    private final static Logger logger = LoggerFactory.getLogger(ReferenceLinkPanel.class);
    
    private class LinkPanel extends Panel {
        public LinkPanel(String id, IModel<String> model, ReferenceLabelModel labelModel) {
            super(id);
            
            final ExternalLink link = new ExternalLink("reference", model);
            link.add(new Label("referenceLabel", labelModel));
            link.add(new AttributeModifier("title", model));

            this.setOutputMarkupId(true);
            this.add(link);
        }
    }
    
    public ReferenceLinkPanel(String id, IModel<Resource> model) {
        super(id, model);
        
        if(model.getObject().hasPersistentIdentifier()) {
            add(new PidPanel("link", new Model<>(model.getObject()), "resource (file or service)"));
        } else {
            // Shared rerence model
            final PropertyModel<String> refModel = new PropertyModel<>(model, "ref");     
            add(new LinkPanel("link", refModel,  new ReferenceLabelModel(model)));
        }

        final PropertyModel<String> descriptionModel = new PropertyModel<>(model, "description");
        add(new Label("description", descriptionModel) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(descriptionModel.getObject() != null);
            }
        });
    }

    /**
     * String model that returns the label of a {@link Resource} if it is
     * available (not null or empty), otherwise the reference URI
     */
    private static class ReferenceLabelModel extends AbstractReadOnlyModel<String> {

        private final IModel<Resource> resourceModel;

        public ReferenceLabelModel(IModel<Resource> resourceModel) {
            this.resourceModel = resourceModel;
        }

        @Override
        public String getObject() {
            final Resource resource = resourceModel.getObject();
            if (resource == null) {
                return null;
            } else {
                if (!Strings.isEmpty(resource.getLabel())) {
                    return resource.getLabel();
                }
                return resource.getRef();
            }
        }

        @Override
        public void detach() {
            resourceModel.detach();
        }

    }

}
