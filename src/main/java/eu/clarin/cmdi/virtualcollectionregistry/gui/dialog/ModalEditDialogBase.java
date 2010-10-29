package eu.clarin.cmdi.virtualcollectionregistry.gui.dialog;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class ModalEditDialogBase<T> extends ModalDialogBase {
    public abstract class ContentPanel extends Panel {
        public ContentPanel(String id) {
            super(id);
        }
        
        public abstract Form<T> getForm();
        
        public abstract FeedbackPanel getFeedbackPanel();
        
    } // class ModalEditDialogBase.ContentPanel
    
    private final class ButtonBar extends Panel {
        public ButtonBar(String id, Form<?> form) {
            super(id);
            addButton = new AjaxButton("addButton",
                    new Model<String>("Add"), form) {
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(contentPanel.getFeedbackPanel());
                    super.onError(target, form);
                }

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ModalEditDialogBase.this.close(target);
                    @SuppressWarnings("unchecked")
                    final T object = (T) form.getModelObject();
                    ModalEditDialogBase.this.onSubmit(target, object);
                }
            };
            add(addButton);
            modifyButton = new AjaxButton("modifyButton",
                    new Model<String>("Modify"), form) {
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(contentPanel.getFeedbackPanel());
                    super.onError(target, form);
                }

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ModalEditDialogBase.this.close(target);
                    @SuppressWarnings("unchecked")
                    final T object = (T) form.getModelObject();
                    ModalEditDialogBase.this.onSubmit(target, object);
                }
            };
            add(modifyButton);
            final AjaxButton cancelButton =
                new AjaxButton("cancelButton",
                        new Model<String>("Cancel"), form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ModalEditDialogBase.this.close(target);
                    @SuppressWarnings("unchecked")
                    final T object = (T) form.getModelObject();
                    ModalEditDialogBase.this.onCancel(target, object);
                }
            };
            cancelButton.setDefaultFormProcessing(false);
            add(cancelButton);
        }
    } // class EditCreatorDialog.ButtonBar

    private ContentPanel contentPanel;
    private AjaxButton addButton;
    private AjaxButton modifyButton;

    public ModalEditDialogBase(String id, IModel<String> title) {
        super(id, title);
    }

    @Override
    protected final Panel createContent(String id) {
        this.contentPanel = createContentPanel(id);
        this.contentPanel.add(new AttributeAppender("class",
                new Model<String>("editDialog"), " "));
        this.contentPanel.getFeedbackPanel().setOutputMarkupId(true);
        return this.contentPanel;
    }

    @Override
    protected final Panel createButtonBar(String id) {
        return new ButtonBar(id, contentPanel.getForm());
    }

    @Override
    public final void show(AjaxRequestTarget target) {
        this.show(target, null);
    }

    public final void show(AjaxRequestTarget target, T object) {
        if (object == null) {
            object = newObjectInstance();
            addButton.setVisible(true);
            modifyButton.setVisible(false);
        } else {
            addButton.setVisible(false);
            modifyButton.setVisible(true);
        }
        contentPanel.getForm().setModel(createModel(object));
        super.show(target);
    }

    protected abstract T newObjectInstance();

    protected abstract IModel<T> createModel(T object);
    
    protected abstract ContentPanel createContentPanel(String id);
    
    public abstract void onSubmit(AjaxRequestTarget target, T object);

    public void onCancel(AjaxRequestTarget target, T object) {
    }

} // class ModalEditDialog
