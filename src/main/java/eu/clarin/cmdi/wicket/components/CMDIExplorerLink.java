package eu.clarin.cmdi.wicket.components;

import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifier;
import eu.clarin.cmdi.wicket.components.pid.PidType;
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

public class CMDIExplorerLink extends AjaxFallbackLink<String> {

    private final static Logger logger = LoggerFactory.getLogger(CMDIExplorerLink.class);

    private final static String TOOLTIP_DOWNLOAD_TEXT = "Download this collection with the CMDI explorer";

    @SpringBean
    private VcrConfig vcrConfig;

    private final IModel<String> urlModel;

    public static CMDIExplorerLink forCollection(String id, VirtualCollection vc, String preferidPidType) {
        String href = vc.getPrimaryIdentifier().getActionableURI();
        logger.info("Select pid of preferred type = {}", preferidPidType);
        if(!preferidPidType.equalsIgnoreCase("primary")) {
            boolean found = false;
            for (PersistentIdentifier pid : vc.getAllIdentifiers()) {
                if(pid.getPidType() == PidType.fromString(preferidPidType)) {
                    href = pid.getActionableURI();
                    found = true;
                }
            }
            if(!found) {
                logger.warn("Did not find PID of prefered type = {} for collection with id = {}", preferidPidType, vc.getId());
            }
        }
        CMDIExplorerLink link = new CMDIExplorerLink(id, Model.of(href));
        UIUtils.addTooltip(link, TOOLTIP_DOWNLOAD_TEXT);
        return link;
    }

    public CMDIExplorerLink(String id, IModel<String> urlModel) {
        super(id);
        this.urlModel = urlModel;
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        final String endpoint = vcrConfig.getDownloadEndpoint();
        final String href = buildCmdiExplorerUrl(endpoint, urlModel.getObject() );
        throw new RedirectToUrlException(href);
    }

    @Override
    public void detachModels() {
        super.detachModels();
        urlModel.detach();
    }

    public String buildCmdiExplorerUrl(String endpoint, String href) {
        try {
            String encodedUrl = URLEncoder.encode(href, "UTF-8");
            return String.format("%s/%s", endpoint, encodedUrl);
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error while creating cmdi explorer link", ex);
            return null;
        }
    }
}
