package eu.clarin.cmdi.virtualcollectionregistry.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.model.IModel;

public class JsonLdHeaderBehavior extends Behavior {

    private final IModel<String> jsonLdContentModel;

    protected JsonLdHeaderBehavior(IModel<String> jsonLdContentModel) {
        this.jsonLdContentModel = jsonLdContentModel;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        final String script = "<script type=\"application/ld+json\">\n"
                + "/*<![CDATA[*/\n"
                + jsonLdContentModel.getObject()
                + "/*]]>*/\n"
                + "</script>\n";
        response.render(new StringHeaderItem(script));
    }

    @Override
    public void detach(Component component) {
        super.detach(component);
        jsonLdContentModel.detach();
    }
}
