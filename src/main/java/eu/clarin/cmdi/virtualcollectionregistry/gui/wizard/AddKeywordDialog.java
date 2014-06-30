package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ModalEditDialogBase;

@SuppressWarnings("serial")
public abstract class AddKeywordDialog extends ModalEditDialogBase<String> {
    private final class Content extends
            ModalEditDialogBase<String>.ContentPanel {
        private final Form<String> form;
        private final FeedbackPanel feedbackPanel;

        public Content(String id, IModel<String> model) {
            super(id);
            form = new Form<String>("addKeywordForm", model);
            final TextField<String> keywordField =
                new RequiredTextField<String>("keyword", form.getModel());
            keywordField.add(new StringValidator.MaximumLengthValidator(255));
            form.add(keywordField);
            feedbackPanel = new FeedbackPanel("feedback");
            form.add(feedbackPanel);
            add(form);
        }

        @Override
        public Form<String> getForm() {
            return form;
        }

        @Override
        public FeedbackPanel getFeedbackPanel() {
            return feedbackPanel;
        }
    } // class EditCreatorDialog.Content

    public AddKeywordDialog(final String id) {
        super(id, new Model<String>("Add Keyword"));
        setInitialWidth(400);
    }

    @Override
    protected ModalEditDialogBase<String>.ContentPanel
        createContentPanel(String id, IModel<String> model) {
        return new Content(id, model);
    }

    @Override
    protected final String newObjectInstance() {
        return new String();
    }

    @Override
    protected final IModel<String> createModel() {
        return new Model<String>();
    }

    @Override
    protected String getCssClass() {
        return "addKeywordDialog";
    }

} // class AddKeywordDialog
