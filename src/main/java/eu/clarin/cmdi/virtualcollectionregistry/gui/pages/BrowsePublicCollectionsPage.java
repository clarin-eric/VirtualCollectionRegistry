package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PublishedCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.VirtualCollectionTable;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.security.Principal;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class BrowsePublicCollectionsPage extends BasePage {

    private class ActionsPanel extends Panel {

        public ActionsPanel(String id, IModel<VirtualCollection> model) {
            super(id, model);
            setRenderBodyOnly(true);

            final AjaxLink<VirtualCollection> detailsLink
                    = new AjaxLink<VirtualCollection>("details", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doDetails(target, getModel());
                        }
                    };
            add(detailsLink);
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
        setResponsePage(VirtualCollectionDetailsPage.class, VirtualCollectionDetailsPage.createPageParameters(vc.getObject(), getPageReference()));
    }

} // class BrowsePublicCollectionsPage
