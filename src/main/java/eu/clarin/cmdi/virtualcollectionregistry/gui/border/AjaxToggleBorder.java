package eu.clarin.cmdi.virtualcollectionregistry.gui.border;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.odlabs.wiquery.core.effects.EffectSpeed;
import org.odlabs.wiquery.core.effects.sliding.SlideToggle;
import org.odlabs.wiquery.core.events.Event;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryEventBehavior;
import org.odlabs.wiquery.core.javascript.JsScope;
import org.odlabs.wiquery.core.javascript.JsStatement;

@SuppressWarnings("serial")
public class AjaxToggleBorder extends Border {
    private final static String COLLAPSED_CLASS = "collapsed";
    
    public AjaxToggleBorder(String id, IModel<String> title,
            final boolean expanded) {
        super(id);
        setRenderBodyOnly(true);

        final WebMarkupContainer header = new WebMarkupContainer("header");
        header.add(new Label("title", title));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        content.add(getBodyContainer());
        add(header);
        add(content);

        header.add(new WiQueryEventBehavior(new Event(MouseEvent.CLICK) {
            @Override
            public JsScope callback() {
                final JsScope cb = JsScope.quickScope(new JsStatement()
                    .$(header).toggleClass(COLLAPSED_CLASS));
                final SlideToggle effect = new SlideToggle(EffectSpeed.SLOW);
                effect.setCallback(cb);
                return JsScope.quickScope(new JsStatement()
                    .$(content).chain(effect));
            }
        }));
        if (!expanded) {
            header.add(new AttributeAppender("class",
                    new Model<String>(COLLAPSED_CLASS), " "));
            content.add(new AttributeAppender("style",
                    new Model<String>("display:none"), ";"));
        }
    }

    public AjaxToggleBorder(String id, IModel<String> title) {
        this(id, title, true);
    }
    
} // class AjaxToggleBorder
