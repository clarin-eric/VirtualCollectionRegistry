/*
 * Copyright (C) 2016 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 *
 * @author wilelb
 */
public abstract class PublishConfirmationDialog extends ModalDialogBase {
       
    private static enum ButtonState { CANCEL, PUBLISH, PUBLISH_FROZEN }
        
    private final class ButtonBar extends Panel {
        public ButtonBar(String id) {
            super(id);
            final Form<Void> form = new Form<Void>("buttonsForm");
            
            final AjaxButton noButton =
                new AjaxButton("noButton", new Model<String>("Cancel"), form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    answer = ButtonState.CANCEL;
                    PublishConfirmationDialog.this.close(target);
                }
            };
            noButton.setDefaultFormProcessing(false);
            form.add(noButton);

            final AjaxButton yesButton =
                new AjaxButton("publishButton", new Model<String>("Publish"), form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    answer = ButtonState.PUBLISH;
                    PublishConfirmationDialog.this.close(target);
                }
            };
            yesButton.setDefaultFormProcessing(false);
            form.add(yesButton);

            final AjaxButton frozenButton =
                new AjaxButton("frozenButton", new Model<String>("Publish Frozen"), form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    answer = ButtonState.PUBLISH_FROZEN;
                    PublishConfirmationDialog.this.close(target);
                }
            };
            frozenButton.setDefaultFormProcessing(false);
            form.add(frozenButton);
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

    private ButtonState answer;
    private MultiLineLabel messageLabel;

    public PublishConfirmationDialog(String id, IModel<String> message,
            final Component updateComponent) {
        super(id, new Model<String>("Please confirm"));
        setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            @Override
            public void onClose(AjaxRequestTarget target) {
                if (answer == ButtonState.PUBLISH || answer == ButtonState.PUBLISH_FROZEN) {
                    onConfirm(target);
                    if (updateComponent != null) {
                        target.add(updateComponent);
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

    public PublishConfirmationDialog(String id, final Component updateComponent) {
        this(id, null, updateComponent);
    }

    public PublishConfirmationDialog(String id) {
        this(id, null, null);
    }

    @Override
    protected Panel createButtonBar(String id) {
        return new ButtonBar(id);
    }

    @Override
    protected Panel createContent(String id) {
        Content content = new Content(id);
        content.add(new AttributeAppender("class", getCssClass(), " "));
        return content;
    }

    protected Model<String> getCssClass() {
        return Model.of("confirmationDialog");
    }

    @Override
    public void show(AjaxRequestTarget target) {
        answer = ButtonState.CANCEL; /* set save default value */
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

    public boolean isFrozen() {
        return answer == ButtonState.PUBLISH_FROZEN;
    }
    
    public abstract void onConfirm(AjaxRequestTarget target);

    public void onCancel(AjaxRequestTarget target) {}   
}
