package eu.clarin.cmdi.virtualcollectionregistry.gui.panels.versions;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.VirtualCollectionDetailsPage;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

public class VersionsPanel extends Panel {

    public VersionsPanel(String id, final IModel<VirtualCollection> model, final PageReference reference) {
        super(id);

        final VirtualCollection vc = model.getObject();
        final Long this_id = vc.getId();
        final List<VirtualCollection> versionList =vc.getAllVersions();
        //WebMarkupContainer versions = new WebMarkupContainer("versions_container");
        add(new Label("versions_lbl","Versions:" ));
        ListView<VirtualCollection> versionListview = new ListView<VirtualCollection>("version_list", versionList) {
            @Override
            protected void populateItem(ListItem<VirtualCollection> item) {
                final VirtualCollection vc = item.getModel().getObject();

                AjaxLink versionDetailsButton = new AjaxLink( "version_link", new Model<String>("") ){
                    @Override
                    public void onClick( AjaxRequestTarget target ) {
                        if(vc.getState() != VirtualCollection.State.DELETED) {
                            setResponsePage(
                                    VirtualCollectionDetailsPage.class,
                                    VirtualCollectionDetailsPage.createPageParameters(
                                            vc, reference, VirtualCollectionDetailsPage.BackPage.PUBLIC_LISTING));
                        }
                    }
                };
                versionDetailsButton.add(new Label("version_link_label", vc.getName()+" ("+vc.getCreationDate()+")"));
                versionDetailsButton.setEnabled(vc.getId() != this_id);
                item.add(versionDetailsButton);
            }
        };
        add(versionListview);
        setVisible(!versionList.isEmpty());
        //add(versions);
    }
}
