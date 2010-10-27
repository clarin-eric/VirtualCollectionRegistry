package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;

@SuppressWarnings("serial")
public abstract class EditCreatorDialog extends ModalWindow {
    private final class Content extends Panel {
        private final Form<Creator> form;

        public Content(String id, final ModalWindow window) {
            super(id);
            form = new Form<Creator>("newCreatorForm",
                    new CompoundPropertyModel<Creator>(new Creator()));
            form.add(new RequiredTextField<String>("name"));
            final TextField<String> emailField = new TextField<String>("email");
            emailField.add(EmailAddressValidator.getInstance());
            form.add(emailField);
            form.add(new TextField<String>("organisation"));
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            form.add(feedback);
            final AjaxButton add = new AjaxButton("add", form) {
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(feedback);
                    super.onError(target, form);
                }

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    window.close(target);
                    EditCreatorDialog.this.onSubmit(target,
                            (Creator) form.getModelObject());
                }
            };
            form.add(add);
            final AjaxButton cancel = new AjaxButton("cancel", form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    window.close(target);
                }
            };
            cancel.setDefaultFormProcessing(false);
            form.add(cancel);
            add(form);
        }
        
        private void updateModel(Creator creator) {
            form.setModel(new CompoundPropertyModel<Creator>(creator));
        }
    } // class CreateVirtualCollectionWizard.CreatorsStep.NewCreatorPanel

    private final Content content;


    public EditCreatorDialog(final String id) {
        super(id);
        setOutputMarkupId(true);
        setTitle(new Model<String>("New Creator"));
        content = new Content(getContentId(), EditCreatorDialog.this);
        content.setOutputMarkupId(true);
        setContent(content);
        setInitialWidth(350);
        setUseInitialHeight(false);
    }

    public void setCreator(Creator creator) {
        if (creator != null) {
            content.updateModel(creator);
        }
    }

    public abstract void onSubmit(AjaxRequestTarget target, Creator creator);

} // EditCreatorDialog
