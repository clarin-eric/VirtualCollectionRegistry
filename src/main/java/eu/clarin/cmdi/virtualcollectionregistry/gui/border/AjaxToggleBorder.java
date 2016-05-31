package eu.clarin.cmdi.virtualcollectionregistry.gui.border;


import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.odlabs.wiquery.core.IWiQueryPlugin;
import org.odlabs.wiquery.core.javascript.JsStatement;

@SuppressWarnings("serial")
public class AjaxToggleBorder extends Border implements IWiQueryPlugin {
    private static final ResourceReference JAVASCRIPT_RESOURCE =
        new PackageResourceReference(AjaxToggleBorder.class, "AjaxToggleBorder.js");
    private final WebMarkupContainer border;

    public AjaxToggleBorder(String id, IModel<String> title,
            boolean expanded, String cssClass) {
        super(id);
        setRenderBodyOnly(true);

        border = new WebMarkupContainer("border");
        border.setOutputMarkupId(true);

        final WebMarkupContainer header = new WebMarkupContainer("header");
        header.add(new Label("title", title));
        border.add(header);

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        content.add(getBodyContainer());
        border.add(content);
        addToBorder(border);
        
        if (!expanded) {
            header.add(new AttributeAppender("class",
                    new Model<String>("collapsed"), " "));
            content.add(new AttributeAppender("style",
                    new Model<String>("display:none"), ";"));
        }
        if (cssClass != null) {
            content.add(new AttributeAppender("class",
                    new Model<String>(cssClass), " "));
        }
    }

    public AjaxToggleBorder(String id, IModel<String> title, String cssClass) {
        this(id, title, true, cssClass);
    }

    public AjaxToggleBorder(String id, IModel<String> title) {
        this(id, title, true, null);
    }

    //TODO:WiQuery
    /*
    @Override
    public void contribute(WiQueryResourceManager manager) {
        manager.addJavaScriptResource(JAVASCRIPT_RESOURCE);
    }
    */
    @Override
    public JsStatement statement() {
        return new JsStatement().$(border).append(".ajaxToggleBorder()");
    }

} // class AjaxToggleBorder
