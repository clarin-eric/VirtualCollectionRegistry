package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfigImpl;
import eu.clarin.cmdi.wicket.components.citation.CitationPanelFactory;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PublishedCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.VirtualCollectionTable;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            
            AjaxLink lrsLink = UIUtils.getLrsRedirectAjaxLink("lrs", model, vcrConfig.getSwitchboardEndpoint());
            lrsLink.setVisible(vcrConfig.isSwitchboardEnabledForCollections());
            add(lrsLink);
        }
        
    } // class BrowsePublicCollectionsPage.ActionsPanel

    public BrowsePublicCollectionsPage() {
        super();
        final PageReference reference = getPageReference();
        final VirtualCollectionTable table
                = new VirtualCollectionTable("collectionsTable", new PublishedCollectionsProvider(), false, false) {
                    @Override
                    protected Panel createActionColumn(String componentId,
                            IModel<VirtualCollection> model) {
                        return new ActionsPanel(componentId, model);
                    }

                    @Override
                    protected Panel createActionPanel(String componentId,
                            IModel<VirtualCollection> model) {
                        return new ActionsPanel(componentId, model);
                    }
                    
                    @Override
                    protected PageReference getPageReference() {
                        return reference;
                    }
                };
        add(table);
    }

    private void doDetails(AjaxRequestTarget target, IModel<VirtualCollection> vc) {
        setResponsePage(
            VirtualCollectionDetailsPage.class, 
            VirtualCollectionDetailsPage.createPageParameters(
                vc.getObject(), 
                getPageReference(), 
                VirtualCollectionDetailsPage.BackPage.PUBLIC_LISTING));
    }

    @Override
    public IModel<String> getCanonicalUrlModel() {
        final CharSequence url = RequestCycle.get().urlFor(getClass(), null);
        final String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
        return new Model(absoluteUrl);
    }
} // class BrowsePublicCollectionsPage
