package eu.clarin.cmdi.virtualcollectionregistry.gui.dialog;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class ConfirmationDialog extends ModalDialogBase {
    private final class ButtonBar extends Panel {
        public ButtonBar(String id) {
            super(id);
            final Form<Void> form = new Form<Void>("buttonsForm");
            final AjaxButton yesButton =
                new AjaxButton("yesButton", new Model<String>("Yes"), form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target,
                        Form<?> form) {
                    answer = true;
                    ConfirmationDialog.this.close(target);
                }
            };
            yesButton.setDefaultFormProcessing(false);
            form.add(yesButton);
            final AjaxButton noButton =
                new AjaxButton("noButton", new Model<String>("No"), form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target,
                        Form<?> form) {
                    answer = false;
                    ConfirmationDialog.this.close(target);
                }
            };
            noButton.setDefaultFormProcessing(false);
            form.add(noButton);
            add(form);
        }
    } // class ConfirmationDialog.ButtonBar

    private final class Content extends Panel {
        private Content(String id) {
            super(id);
            messageLabel = new MultiLineLabel("message");
            add(messageLabel);
        }
    } // class ConfirmationDialog.Content

    private boolean answer;
    private MultiLineLabel messageLabel;

    public ConfirmationDialog(String id, IModel<String> message,
            final Component updateComponent) {
        super(id, new Model<String>("Please confirm"));
        setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            @Override
            public void onClose(AjaxRequestTarget target) {
                if (answer) {
                    onConfirm(target);
                    if (updateComponent != null) {
                        target.addComponent(updateComponent);
                    }
                } else {
                    onCancel(target);
                }
            }
        });
        if (message != null) {
            setMessage(message);
        }
    }

    public ConfirmationDialog(String id, final Component updateComponent) {
        this(id, null, updateComponent);
    }

    public ConfirmationDialog(String id) {
        this(id, null, null);
    }

    @Override
    protected Panel createButtonBar(String id) {
        return new ButtonBar(id);
    }

    @Override
    protected Content createContent(String id) {
        Content content = new Content(id);
        content.add(new AttributeAppender("class",
                new Model<String>("confirmationDialog"), " "));
        return content;
    }

    @Override
    public void show(AjaxRequestTarget target) {
        answer = false; /* set save default value */
        super.show(target);
    }

    public void show(AjaxRequestTarget target, IModel<String> message) {
        setMessage(message);
        this.show(target);
    }

    public void setMessage(IModel<String> message) {
        if (message == null) {
            throw new NullPointerException("message == null");
        }
        messageLabel.setDefaultModel(message);
    }

    public abstract void onConfirm(AjaxRequestTarget target);

    public void onCancel(AjaxRequestTarget target) {
    }

} // abstract class ConfirmationDialog
