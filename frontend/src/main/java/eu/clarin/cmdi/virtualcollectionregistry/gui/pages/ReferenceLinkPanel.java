package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
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

        Label lblOrigin = new Label("origin", "Original data catalogue: "+model.getObject().getOrigin());
        lblOrigin.setVisible( model.getObject().getOrigin() != null && ! model.getObject().getOrigin().isEmpty());
        add(lblOrigin);

        boolean originVisibility = model.getObject().getOriginalQuery() != null && ! model.getObject().getOriginalQuery().isEmpty();
        Label labelOriginalQuery = new Label("originalQueryLinkLabel", "Original query:");
        labelOriginalQuery.setVisible(originVisibility);
        add(labelOriginalQuery);

        ExternalLink linkOriginalQuery = new ExternalLink("originalQueryLink", model.getObject().getOriginalQuery());
        linkOriginalQuery.add(new Label("originalQueryLinkValue", model.getObject().getOriginalQuery()));
        linkOriginalQuery.setVisible(originVisibility);
        add(linkOriginalQuery);

        final PropertyModel<String> refModel = new PropertyModel<>(model, "ref");
        add(new LinkPanel("link", refModel,  new ReferenceLabelModel(model)));

        PidPanel pPanel = new PidPanel("pid", new Model<>(model.getObject()), "resource (file or service)");
        pPanel.setVisible(model.getObject().hasPersistentIdentifier());
        add(pPanel);

        String htmlValue = "";
        if(model.getObject().getDescription() != null) {
            MutableDataSet options = new MutableDataSet();
            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();
            Node document = parser.parse(model.getObject().getDescription());
            htmlValue = renderer.render(document);
        }

        Label lbl = new Label("description", Model.of(htmlValue)) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(model.getObject().getDescription() != null);
            }
        };
        lbl.setEscapeModelStrings(false);
        add(lbl);
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
