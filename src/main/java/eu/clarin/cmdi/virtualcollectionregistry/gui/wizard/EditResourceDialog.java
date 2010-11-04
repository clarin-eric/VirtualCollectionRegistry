package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ModalEditDialogBase;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;

@SuppressWarnings("serial")
public abstract class EditResourceDialog extends ModalEditDialogBase<Resource> {
    private final class Content extends
            ModalEditDialogBase<Resource>.ContentPanel {
        private final Form<Resource> form;
        private final FeedbackPanel feedbackPanel;
        
        public Content(String id, IModel<Resource> model) {
            super(id);
            form = new Form<Resource>("editResourceForm", model);
            final DropDownChoice<Resource.Type> typeChoice =
                new DropDownChoice<Resource.Type>("type",
                        Arrays.asList(Resource.Type.values()),
                        new EnumChoiceRenderer<Resource.Type>(this));
            typeChoice.setRequired(true);
            form.add(typeChoice);
            final TextField<String> refField =
                new RequiredTextField<String>("ref");
            refField.add(new StringValidator.MaximumLengthValidator(255));
            refField.add(new UrlValidator(UrlValidator.NO_FRAGMENTS));
            form.add(refField);
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
        super(id, new Model<String>("Add/Edit Resource"));
        setInitialWidth(600);
    }

    @Override
    protected ModalEditDialogBase<Resource>.ContentPanel
        createContentPanel(String id, IModel<Resource> model) {
        return new Content(id, model);
    }

    @Override
    protected final Resource newObjectInstance() {
        return new Resource();
    }
    
    @Override
    protected final IModel<Resource> createModel() {
        return new CompoundPropertyModel<Resource>(null);
    }

    @Override
    protected String getCssClass() {
        return "editResourceDialog";
    }

} // EditResourceDialog
