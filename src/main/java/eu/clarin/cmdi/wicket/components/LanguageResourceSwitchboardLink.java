package eu.clarin.cmdi.wicket.components;

import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LanguageResourceSwitchboardLink extends AjaxFallbackLink<String> {

    private final static Logger logger = LoggerFactory.getLogger(LanguageResourceSwitchboardLink.class);

    private final static String TOOLTIP_COLLECTION_TEXT = "Process this collection with the language resource switchboard";
    private final static String TOOLTIP_RESOURCE_TEXT = "Process this resource with the language resource switchboard";

    @SpringBean
    private VcrConfig vcrConfig;

    private final IModel<String> urlToProcessModel;
    private final IModel<String> mimeTypeModel;
    private final IModel<String> languageCodeModel;

    public static LanguageResourceSwitchboardLink forResource(String id, Resource resource) {
        String ref = resource.getRef();
        if (resource.hasPersistentIdentifier()) {
            ref = resource.getPidUri();
        }
        LanguageResourceSwitchboardLink link =
            new LanguageResourceSwitchboardLink(id,
                Model.of(ref),
                Model.of("application/xml"),
                Model.of("en"));
        UIUtils.addTooltip(link, TOOLTIP_RESOURCE_TEXT);
        return link;
    }

    public static LanguageResourceSwitchboardLink forCollection(String id, VirtualCollection vc) {
        LanguageResourceSwitchboardLink link =
            new LanguageResourceSwitchboardLink(id,
                Model.of(vc.getPrimaryIdentifier().getActionableURI()),
                Model.of("application/xml"),
                Model.of("en"));
        UIUtils.addTooltip(link, TOOLTIP_RESOURCE_TEXT);
        return link;
    }

    public LanguageResourceSwitchboardLink(String id, IModel<String> urlToProcessModel, IModel<String> mimeTypeModel, IModel<String> languageCodeModel) {
        super(id);
        this.urlToProcessModel = urlToProcessModel;;
        this.mimeTypeModel = mimeTypeModel;
        this.languageCodeModel = languageCodeModel;

        setOutputMarkupId(true);
        if(isPopupEnabled()) {
            final String onClickJs = String.format(
                "showSwitchboardPopup({'alignSelector': '%s', 'alignRight': true}, {'url': '%s'}); return false;",
                getAlignSelectorJs(),
                this.urlToProcessModel.getObject());
            add(new AttributeModifier("onclick", onClickJs));
        }
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        if (target == null || !isPopupEnabled()) {
            final String endpoint = vcrConfig.getProcessEndpoint();
            final String href = buildSwitchboardUrl(endpoint, urlToProcessModel.getObject(), mimeTypeModel.getObject(), languageCodeModel.getObject() );
            throw new RedirectToUrlException(href);
        }
    }

    @Override
    public void detachModels() {
        super.detachModels();
        urlToProcessModel.detach();
        mimeTypeModel.detach();
        languageCodeModel.detach();
    }

    protected String getAlignSelectorJs() {
        return String.format("#%s", this.getMarkupId());
    }

    protected boolean isPopupEnabled() {
        return vcrConfig.isProcessPopupEnabled();
    }

    public static String buildSwitchboardUrl(String switchboardEndpoint, String href, String mimeType, String languageCode) {
        try {
            return String.format("%s/%s/%s/%s",
                    switchboardEndpoint,
                    URLEncoder.encode(href, "UTF-8"),
                    URLEncoder.encode(mimeType, "UTF-8"),
                    languageCode);
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error while creating switchboard link", ex);
            return null;
        }
    }
}
