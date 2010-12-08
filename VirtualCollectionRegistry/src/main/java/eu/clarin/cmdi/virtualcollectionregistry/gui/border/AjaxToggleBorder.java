package eu.clarin.cmdi.virtualcollectionregistry.gui.border;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class AjaxToggleBorder extends Border {

    public AjaxToggleBorder(String id, IModel<String> title, final boolean expanded) {
        super(id);
        setRenderBodyOnly(true);

        final WebMarkupContainer header = new WebMarkupContainer("header");
        header.add(new Label("title", title));
        header.add(new AttributeAppender("class",
                new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return expanded ? "shown" : "hidden";
                    }
                }, " "));
        add(header);
        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.add(getBodyContainer());
        add(content);
    }

    public AjaxToggleBorder(String id, IModel<String> title) {
        this(id, title, true);
    }
    
} // class AjaxToggleBorder
