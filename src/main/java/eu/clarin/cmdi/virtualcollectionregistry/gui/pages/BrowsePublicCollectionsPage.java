package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.citation.CitationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PublishedCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.VirtualCollectionTable;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class BrowsePublicCollectionsPage extends BasePage {

    private static Logger logger = LoggerFactory.getLogger(BrowsePublicCollectionsPage.class);
    
    private class ActionsPanel extends Panel {

        public ActionsPanel(String id, IModel<VirtualCollection> model) {
            super(id, model);
            setRenderBodyOnly(true);

            final CitationDialog citationDialog = new CitationDialog("citationDialog", model);
            add(citationDialog);
            
            final AjaxLink<VirtualCollection> citeLink
                    = new AjaxLink<VirtualCollection>("cite", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            citationDialog.show(target);
                        }
                    };
            UIUtils.addTooltip(citeLink, "Cite this collection");
            citeLink.setEnabled(model.getObject().isCiteable());
            add(citeLink);
            
            final AjaxLink<VirtualCollection> lrsLink
                    = new AjaxLink<VirtualCollection>("lrs", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            throw new RedirectToUrlException(getLanguageSwitchboardUrl(model.getObject()));
                        }
                    };
            UIUtils.addTooltip(lrsLink, "Open this collection in the language resurce switchboard");
            add(lrsLink);
            
            final AjaxLink<VirtualCollection> detailsLink
                    = new AjaxLink<VirtualCollection>("details", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doDetails(target, getModel());
                        }
                    };
            UIUtils.addTooltip(detailsLink, "View collection details");
            add(detailsLink);
            
            
        }
        
        private String getLanguageSwitchboardUrl(VirtualCollection vc) {
            try {
                //create link for this resource to the language resource switchboard
                final String href = "http://localhost:8080/vcr/service/virtualcollections/"+vc.getId();
                final String mimeType =  "application/xml";
                final String languageCode = "en";
                return String.format("%s#/vlo/%s/%s/%s",
                        "http://weblicht.sfs.uni-tuebingen.de/clrs/",
                        URLEncoder.encode(href, "UTF-8"),
                        URLEncoder.encode(mimeType, "UTF-8"), languageCode);
            } catch (UnsupportedEncodingException ex) {
                logger.error("Error while creating switchboard link", ex);
                return null;
            }
        }

    } // class BrowsePublicCollectionsPage.ActionsPanel

    public BrowsePublicCollectionsPage() {
        super();
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
