package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ModalEditDialogBase;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;

@SuppressWarnings("serial")
public abstract class EditCreatorDialog extends ModalEditDialogBase<Creator> {
    private final class Content extends
            ModalEditDialogBase<Creator>.ContentPanel {
        private final Form<Creator> form;
        private final FeedbackPanel feedbackPanel;
        
        public Content(String id, IModel<Creator> model) {
            super(id);
            form = new Form<Creator>("editCreatorForm", model);
            final TextField<String> nameField =
                new RequiredTextField<String>("name");
            nameField.add(new StringValidator.MaximumLengthValidator(255));
            form.add(nameField);
            final TextField<String> emailField =
                new TextField<String>("email");
            emailField.add(new StringValidator.MaximumLengthValidator(255));
            emailField.add(EmailAddressValidator.getInstance());
            form.add(emailField);
            final TextField<String> organisationField =
                new TextField<String>("organisation");
            organisationField.add(
                    new StringValidator.MaximumLengthValidator(255));
            form.add(organisationField);
            feedbackPanel = new FeedbackPanel("feedback");
            form.add(feedbackPanel);
            add(form);
        }

        @Override
        public Form<Creator> getForm() {
            return form;
        }

        @Override
        public FeedbackPanel getFeedbackPanel() {
            return feedbackPanel;
        }
    } // class EditCreatorDialog.Content

    public EditCreatorDialog(final String id) {
        super(id, new Model<String>("Add/Edit Creator"));
        setInitialWidth(400);
    }

    @Override
    protected ModalEditDialogBase<Creator>.ContentPanel
        createContentPanel(String id, IModel<Creator> model) {
        return new Content(id, model);
    }

    @Override
    protected final Creator newObjectInstance() {
        return new Creator();
    }
    
    @Override
    protected final IModel<Creator> createModel() {
        return new CompoundPropertyModel<Creator>(null);
    }

    @Override
    protected String getCssClass() {
        return "editCreatorDialog";
    }

} // EditCreatorDialog
