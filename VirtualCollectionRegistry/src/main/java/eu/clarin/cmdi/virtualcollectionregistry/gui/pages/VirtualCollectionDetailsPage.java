package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;


@SuppressWarnings("serial")
public class VirtualCollectionDetailsPage extends BasePage {
    public VirtualCollectionDetailsPage(final VirtualCollection xvc,
            final Page previousPage) {
        super();
        if (previousPage == null) {
            throw new IllegalArgumentException("previousPage == null");
        }
        final Link<Void> backLink = new Link<Void>("back") {
            @Override
            public void onClick() {
                setResponsePage(previousPage);
            }
        };
        add(backLink);
        
        try {
            // FIXME: better use detachable model for collection list?
            final VirtualCollectionRegistry vcr =
                VirtualCollectionRegistry.instance();
            final VirtualCollection vc =
                vcr.retrieveVirtualCollection(xvc.getId());
            setDefaultModel(new CompoundPropertyModel<VirtualCollection>(vc));
            add(new Label("name"));
            add(new Label("type"));
            add(new Label("creationDate"));
            add(new MultiLineLabel("description"));
            add(new Label("purpose"));
            add(new Label("reproducibility"));
            add(new Label("reproducibilityNotice"));
            add(new ListView<String>("keywords") {
                @Override
                protected void populateItem(ListItem<String> item) {
                    item.add(new Label("keyword", item.getModelObject()));
                }
            });
            add(new ListView<Creator>("creators") {
                @Override
                protected void populateItem(ListItem<Creator> item) {
                    final Creator c = item.getModelObject();
                    item.add(new Label("person", c.getPerson()));
                    item.add(new Label("address", c.getAddress()));
                    item.add(new Label("organisation", c.getOrganisation()));
                    item.add(new Label("email", c.getEMail()));
                    item.add(new Label("telephone", c.getTelephone()));
                    item.add(new Label("website", c.getWebsite()));
                    item.add(new Label("role", c.getRole()));
                }
            });
            add(new ListView<Resource>("resources") {
                @Override
                protected void populateItem(ListItem<Resource> item) {
                    final Resource r = item.getModelObject();
                    item.add(new Label("type", r.getType().toString()));
                    item.add(new Label("ref", r.getRef()));
                }
            });
            add(new Label("generatedBy.description"));
            add(new Label("generatedBy.uri"));
            add(new Label("generatedBy.query.profile"));
            add(new Label("generatedBy.query.value"));
        } catch (VirtualCollectionRegistryException e) {
            throw new WicketRuntimeException("fail", e);
        }
    }

} // class VirtualCollectionDetailsPage
