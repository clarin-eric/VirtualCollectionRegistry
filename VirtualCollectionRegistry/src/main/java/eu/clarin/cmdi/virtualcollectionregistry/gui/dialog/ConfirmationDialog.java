package eu.clarin.cmdi.virtualcollectionregistry.gui.dialog;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class ConfirmationDialog extends ModalWindow {
    private class Answer implements Serializable {
        private boolean answer = false;
 
        private Answer() {
            super();
        }
 
        public boolean getAnswer() {
            return answer;
        }
 
        public void setAnswer(boolean answer) {
            this.answer = answer;
        }

    } // class ConfirmationDialog.Answer

    private class Content extends Panel {
        private final MultiLineLabel messageLabel;
        
        private Content(String id, IModel<String> message,
                final ModalWindow window, final Answer answer) {
            super(id);
     
            final Form<Object> yesNoForm =
                new Form<Object>("form");
            messageLabel = new MultiLineLabel("message", message);
            yesNoForm.add(messageLabel);
            final AjaxButton yesButton =
                new AjaxButton("yesButton", yesNoForm) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    if (target != null) {
                        answer.setAnswer(true);
                        window.close(target);
                    }
                }
            };
            yesNoForm.add(yesButton);
            final AjaxButton noButton =
                new AjaxButton("noButton", yesNoForm) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    if (target != null) {
                        answer.setAnswer(false);
                        window.close(target);
                    }
                }
            };
            yesNoForm.add(noButton);
            add(yesNoForm);
        }
        
        private void updateMessage(IModel<String> message) {
            messageLabel.setDefaultModel(message);
        }
    } // class ConfirmationDialog.Content
    
    private final Content content;
    private final Answer answer = new Answer();

    public ConfirmationDialog(final String id, IModel<String> message) {
        super(id);
        if (message == null) {
            throw new NullPointerException("message == null");
        }
        answer.setAnswer(false);
        setOutputMarkupId(true);
        setTitle(new Model<String>("Please confirm"));
        content =
            new Content(this.getContentId(), message, this, answer);
        content.setOutputMarkupId(true);
        setContent(content);
        setInitialWidth(350);
        setInitialHeight(200);
        setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            @Override
            public void onClose(AjaxRequestTarget target) {
                if (answer.getAnswer()) {
                    onConfirm(target);
                } else {
                    onCancel(target);
                }
            }
        });
    }

    public void setMessage(IModel<String> message) {
        content.updateMessage(message);
    }

    public abstract void onConfirm(AjaxRequestTarget target);

    public abstract void onCancel(AjaxRequestTarget target);

} // abstract class ConfirmationDialog
