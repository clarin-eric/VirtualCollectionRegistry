package eu.clarin.cmdi.wicket.components;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PidType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
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

    public static Component forResource(String id, Resource resource) {
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

    public static Component forCollection(String id, VirtualCollection vc, VcrConfig vcrConfig) {
        if(vc.getPrimaryIdentifier() == null) {
            WebMarkupContainer container = new WebMarkupContainer(id);
            container.setVisible(false);
            return container;
        }
        String href = vc.getPrimaryIdentifier().getActionableURI();
        if(!vcrConfig.getProcessEndpointPreferedPidType().equalsIgnoreCase("primary")) {
            boolean found = false;
            for (PersistentIdentifier pid : vc.getAllIdentifiers()) {
                if(pid.getPidType() == PidType.fromString(vcrConfig.getProcessEndpointPreferedPidType())) {
                    href = pid.getActionableURI();
                    found = true;
                }
            }
            /*
            if(!found) {
                logger.trace("Did not find PID of prefered type = {} for collection with id = {}", vcrConfig.getProcessEndpointPreferedPidType(), vc.getId());
            }
             */
        }

        LanguageResourceSwitchboardLink link =
            new LanguageResourceSwitchboardLink(id,
                Model.of(href),
                Model.of("application/xml"),
                Model.of("en"));
        UIUtils.addTooltip(link, TOOLTIP_RESOURCE_TEXT);
        link.setVisible(vcrConfig.isProcessEnabledForCollections());
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
