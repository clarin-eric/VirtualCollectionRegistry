package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class ReferenceLinkPanel extends Panel {

    public ReferenceLinkPanel(String id, IModel<Resource> model) {
        super(id, model);

        // Shared rerence model
        final PropertyModel<String> refModel = new PropertyModel<>(model, "ref");

        // Wrapper for link model that detects handles
        final HandleLinkModel linkModel = new HandleLinkModel(refModel);
        final ExternalLink link = new ExternalLink("reference", linkModel);
        link.add(new AttributeModifier("title", refModel));

        // Set label on link
        final ReferenceLabelModel labelModel = new ReferenceLabelModel(model);
        link.add(new Label("referenceLabel", labelModel));

        add(link);

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
