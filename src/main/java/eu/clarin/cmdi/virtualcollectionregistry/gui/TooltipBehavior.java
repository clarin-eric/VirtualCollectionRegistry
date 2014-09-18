package eu.clarin.cmdi.virtualcollectionregistry.gui;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.model.IModel;
import org.odlabs.wiquery.core.behavior.WiQueryAbstractBehavior;
import org.odlabs.wiquery.core.commons.WiQueryResourceManager;
import org.odlabs.wiquery.core.javascript.JsQuery;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.core.options.DefaultOptionsRenderer;
import org.odlabs.wiquery.core.options.IOptionsRenderer;
import org.odlabs.wiquery.core.options.Options;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class TooltipBehavior extends WiQueryAbstractBehavior {

    public static final ResourceReference QTIP_JAVASCRIPT_RESOURCE
            = new JavascriptResourceReference(TooltipBehavior.class, "jquery.qtip-1.0.0-rc3.min.js");

    private final IModel<String> tooltipModel;

    public TooltipBehavior(IModel<String> tooltipModel) {
        this.tooltipModel = tooltipModel;
    }

    @Override
    public void contribute(WiQueryResourceManager wiQueryResourceManager) {
        wiQueryResourceManager.addJavaScriptResource(QTIP_JAVASCRIPT_RESOURCE);
    }

    @Override
    public JsStatement statement() {
        return new JsQuery(getComponent()).$().chain("qtip", getOptions().getJavaScriptOptions());
    }

    public Options getOptions() {
        final Options options = new Options();
        options.setRenderer(optionsRenderer);
        options.putString("content", tooltipModel);
        options.put("show", getShowEvent());
        options.put("hide", getHideEvent());
        return options;
    }

    public String getShowEvent() {
        return "mouseover";
    }

    public String getHideEvent() {
        return "mouseout";
    }

    private final IOptionsRenderer optionsRenderer = new IOptionsRenderer() {

        @Override
        public void renderBefore(StringBuilder stringBuilder) {
            DefaultOptionsRenderer.get().renderBefore(stringBuilder);
        }

        @Override
        public CharSequence renderOption(String name, Object value, boolean isLast) {
            final Object newValue;
            if (value instanceof CharSequence) {
                newValue = String.format("'%s'", value);
            } else {
                newValue = value;
            }
            return DefaultOptionsRenderer.get().renderOption(name, newValue, isLast);
        }

        @Override
        public void renderAfter(StringBuilder stringBuilder) {
            DefaultOptionsRenderer.get().renderAfter(stringBuilder);
        }
    };
//
//    private static class StringQuoteModel extends AbstractReadOnlyModel<String> {
//
//        private final IModel<String> wrappedModel;
//
//        public StringQuoteModel(IModel<String> wrappedModel) {
//            this.wrappedModel = wrappedModel;
//        }
//
//        @Override
//        public String getObject() {
//            return String.format("'%s'", wrappedModel.getObject());
//        }
//
//    }

}
