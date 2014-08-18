package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import eu.clarin.cmdi.virtualcollectionregistry.service.impl.ReferenceValidator;
import eu.clarin.cmdi.virtualcollectionregistry.gui.VolatileEntityModel;
import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ModalEditDialogBase;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import org.apache.wicket.markup.html.form.TextArea;

@SuppressWarnings("serial")
public abstract class EditResourceDialog extends ModalEditDialogBase<Resource> {

    private final class Content extends
            ModalEditDialogBase<Resource>.ContentPanel {

        private final Form<Resource> form;
        private final FeedbackPanel feedbackPanel;

        public Content(String id, IModel<Resource> model) {
            super(id);
            form = new Form<>("editResourceForm", model);

            form.add(new DropDownChoice<>("type",
                    Arrays.asList(Resource.Type.values()),
                    new EnumChoiceRenderer<Resource.Type>(this))
                    .setRequired(true)
            );

            form.add(new RequiredTextField<String>("ref")
                    .add(new StringValidator.MaximumLengthValidator(255))
                    .add(new ReferenceValidator()));

            form.add(new TextField<String>("label")
                    .add(new StringValidator.MaximumLengthValidator(255)));

            form.add(new TextArea<String>("description"));

            feedbackPanel = new FeedbackPanel("feedback");
            form.add(feedbackPanel);
            add(form);
        }

        @Override
        public Form<Resource> getForm() {
            return form;
        }

        @Override
        public FeedbackPanel getFeedbackPanel() {
            return feedbackPanel;
        }
    } // class EditResourceDialog.Content

    public EditResourceDialog(final String id) {
        super(id, Model.of("Add/Edit Resource"));
        setInitialWidth(600);
    }

    @Override
    protected ModalEditDialogBase<Resource>.ContentPanel
            createContentPanel(String id, IModel<Resource> model) {
        return new Content(id, model);
    }

    @Override
    protected final IModel<Resource> newInstanceModel() {
        return new VolatileEntityModel<>(new Resource());
    }

    @Override
    protected final IModel<Resource> createEmptyModel() {
        return new VolatileEntityModel<>(null);
    }

    @Override
    protected String getCssClass() {
        return "editResourceDialog";
    }

} // EditResourceDialog
