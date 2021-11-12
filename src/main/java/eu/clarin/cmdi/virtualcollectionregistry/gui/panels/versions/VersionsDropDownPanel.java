package eu.clarin.cmdi.virtualcollectionregistry.gui.panels.versions;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.VirtualCollectionDetailsPage;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VersionsDropDownPanel extends Panel {

    private final Logger logger = LoggerFactory.getLogger(VersionsDropDownPanel.class);

    private class IdValueOption implements Serializable {
        private final Long id;
        private final String value;

        public IdValueOption (final Long id, final String value) {
            this.id = id;
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    public VersionsDropDownPanel(String id, final IModel<VirtualCollection> model, final PageReference reference) {
        super(id);

        final VirtualCollection vc = model.getObject();
        final Long this_id = vc.getId();
        final List<VirtualCollection> versionList = vc.getAllVersions();
        final List<IdValueOption> versionListOptions = new ArrayList<>();
        for(VirtualCollection vcVersion : versionList) {
            versionListOptions.add(
                new IdValueOption(
                    vcVersion.getId(),
                    vcVersion.getName()+" ("+vcVersion.getCreationDate()+")"));
        }
        add(new Label("versions_lbl","Versions:" ));
        ChoiceRenderer choiceRenderer = new ChoiceRenderer("value", "id");

        DropDownChoice<IdValueOption> dropdown = new DropDownChoice("cb_versions", model,versionListOptions, choiceRenderer);
        dropdown.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                final IdValueOption IdValueOption = dropdown.getModelObject();
                logger.info("Selected: id={}, value={}", IdValueOption.getId(), IdValueOption.getValue());
                    setResponsePage(
                        VirtualCollectionDetailsPage.class,
                        VirtualCollectionDetailsPage.createPageParameters(
                            IdValueOption.getId(), reference, VirtualCollectionDetailsPage.BackPage.PUBLIC_LISTING));
                }
        });

        add(dropdown);


    }
}
