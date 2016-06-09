package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.UrlValidator;

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
            form = new Form<>("editCreatorForm", model);
            final TextField<String> personField = new RequiredTextField<>("person");
            personField.add(Application.MAX_LENGTH_VALIDATOR);
            form.add(personField);
            final TextArea<String> addressArea = new TextArea<>("address");
            addressArea.add(Application.MAX_LENGTH_VALIDATOR);
            form.add(addressArea);
            final TextField<String> emailField = new TextField<>("email");
            emailField.add(Application.MAX_LENGTH_VALIDATOR);
            emailField.add(EmailAddressValidator.getInstance());
            form.add(emailField);
            final TextField<String> organisationField = new TextField<>("organisation");
            organisationField.add(Application.MAX_LENGTH_VALIDATOR);
            form.add(organisationField);
            final TextField<String> telephoneField = new TextField<>("telephone");
            telephoneField.add(Application.MAX_LENGTH_VALIDATOR);
            form.add(telephoneField);
            final TextField<String> websiteField = new TextField<>("website");
            websiteField.add(Application.MAX_LENGTH_VALIDATOR);
            websiteField.add(new UrlValidator());
            form.add(websiteField);
            final TextField<String> roleField = new TextField<>("role");
            roleField.add(Application.MAX_LENGTH_VALIDATOR);
            form.add(roleField);
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
    protected final IModel<Creator> newInstanceModel() {
        return new CompoundPropertyModel<>(new Creator());
    }

    @Override
    protected final IModel<Creator> createEmptyModel() {
        return new CompoundPropertyModel<>((Creator)null);
    }

    @Override
    protected String getCssClass() {
        return "editCreatorDialog";
    }

} // EditCreatorDialog
