package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.CreateAndEditVirtualCollectionPageV2;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class NoCollectionsPanel extends Panel {
    public NoCollectionsPanel(String id) {
        super(id);

        add(new Label("jumbotron_title", Model.of("CLARIN Virtual Collection Registry")));
        add(new MultiLineLabel("jumbotron_body", Model.of("Welcome to the VCR!\n\nThe registry does not contain any public collections.\nPlease use the button below to start creating  and publishing the first collection.")));

        final AjaxLink<VirtualCollection> editLink = new AjaxLink<VirtualCollection>("jumbotron_btn") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                doCreate(target);
            }
        };
        editLink.add(new Label("jumbotron_btn_lbl", Model.of("Create Collection")));
        UIUtils.addTooltip(editLink, "Create a new virtual collection");
        add(editLink);
    }

    private void doCreate(AjaxRequestTarget target) {
        setResponsePage(CreateAndEditVirtualCollectionPageV2.class);
    }
}
