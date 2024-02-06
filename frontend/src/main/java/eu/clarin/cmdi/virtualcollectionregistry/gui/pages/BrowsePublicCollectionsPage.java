package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfigImpl;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.CollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.rest.RestUtils;
import eu.clarin.cmdi.wicket.components.CMDIExplorerLink;
import eu.clarin.cmdi.wicket.components.LanguageResourceSwitchboardLink;
import eu.clarin.cmdi.wicket.components.citation.CitationPanelFactory;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PublishedCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.VirtualCollectionTable;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("serial")
public class BrowsePublicCollectionsPage extends BasePage {

    private static Logger logger = LoggerFactory.getLogger(BrowsePublicCollectionsPage.class);
    
    @SpringBean
    private VcrConfigImpl vcrConfig;

    private class ActionsPanel extends Panel {

        public ActionsPanel(String id, IModel<VirtualCollection> model) {
            super(id, model);
            setRenderBodyOnly(true);
            add(CitationPanelFactory.getCitationPanel("cite", model, true));
            add(LanguageResourceSwitchboardLink.forCollection("lrs", model.getObject(), vcrConfig));
            add(CMDIExplorerLink.forCollection("download", model.getObject(), vcrConfig));
        }
        
    } // class BrowsePublicCollectionsPage.ActionsPanel

    public BrowsePublicCollectionsPage() {
        super();

        //Enable content negotiation based redirect
        String redirectLocation = "/service/v1/collections/";
        RestUtils.checkRestApiRedirection((HttpServletRequest)getRequest().getContainerRequest(), redirectLocation);

        final PageReference reference = getPageReference();
        final CollectionsProvider provider = new PublishedCollectionsProvider();
        if(provider.size() <= 0) {
            add(new NoCollectionsPanel("collectionsTable"));
        } else {
            final VirtualCollectionTable table
                    = new VirtualCollectionTable("collectionsTable", provider, false, false, timerManager) {
                @Override
                protected Panel createActionColumn(String componentId,
                                                   IModel<VirtualCollection> model) {
                    return new ActionsPanel(componentId, model);
                }
/*
                @Override
                protected Panel createActionPanel(String componentId,
                                                  IModel<VirtualCollection> model) {
                    return new ActionsPanel(componentId, model);
                }
*/
                @Override
                protected PageReference getPageReference() {
                    return reference;
                }
            };
            add(table);
        }
    }
/*
    private void doDetails(AjaxRequestTarget target, IModel<VirtualCollection> vc) {
        setResponsePage(
            VirtualCollectionDetailsPage.class, 
            VirtualCollectionDetailsPage.createPageParameters(
                vc.getObject(), 
                getPageReference(), 
                VirtualCollectionDetailsPage.BackPage.PUBLIC_LISTING));
    }
*/
    @Override
    public IModel<String> getCanonicalUrlModel() {
        final CharSequence url = RequestCycle.get().urlFor(getClass(), null);
        final String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
        return new Model(absoluteUrl);
    }
} // class BrowsePublicCollectionsPage
