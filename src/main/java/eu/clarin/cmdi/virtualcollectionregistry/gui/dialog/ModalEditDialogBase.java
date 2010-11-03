package eu.clarin.cmdi.virtualcollectionregistry.gui.dialog;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class ModalEditDialogBase<T> extends ModalDialogBase {
    public abstract class ContentPanel extends Panel {
        protected ContentPanel(String id) {
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

                @SuppressWarnings("unchecked")
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ModalEditDialogBase.this.doSubmit(target, (Form<T>) form);
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

                @SuppressWarnings("unchecked")
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ModalEditDialogBase.this.doSubmit(target, (Form<T>) form);
                }
            };
            add(modifyButton);
            final AjaxButton cancelButton =
                new AjaxButton("cancelButton",
                        new Model<String>("Cancel"), form) {
                @SuppressWarnings("unchecked")
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ModalEditDialogBase.this.doCancel(target, (Form<T>) form);
                }
            };
            cancelButton.setDefaultFormProcessing(false);
            add(cancelButton);
        }
    } // class EditCreatorDialog.ButtonBar

    private ContentPanel contentPanel;
    private AjaxButton addButton;
    private AjaxButton modifyButton;

    protected ModalEditDialogBase(String id, IModel<String> title) {
        super(id, title);
    }

    @Override
    protected final Panel createContent(String id) {
        final IModel<T> model = createModel();
        contentPanel = createContentPanel(id, model);
        contentPanel.add(new AttributeAppender("class",
                new AbstractReadOnlyModel<String>() {
                    public String getObject() {
                        final String clazz = getCssClass();
                        if ((clazz != null) && !clazz.isEmpty()) {
                            StringBuilder sb = new StringBuilder("editDialog");
                            sb.append(' ');
                            sb.append(clazz);
                            return sb.toString();
                        } else {
                            return "editDialog";
                        }
                    }
                }, " "));
        contentPanel.getForm().removePersistentFormComponentValues(true);
        contentPanel.getFeedbackPanel().setOutputMarkupId(true);
        return contentPanel;
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
        contentPanel.getForm().setModelObject(object);
        super.show(target);
    }

    private final void doSubmit(AjaxRequestTarget target, Form<T> form) {
        close(target);
        final T object = form.getModelObject();
        form.setModelObject(null);
        onSubmit(target, object);
    }
    
    private final void doCancel(AjaxRequestTarget target, Form<T> form) {
        close(target);
        final T object = form.getModelObject();
        form.setModelObject(null);
        onCancel(target, object);
    }

    protected String getCssClass() {
        return null;
    }

    protected abstract T newObjectInstance();

    protected abstract IModel<T> createModel();

    protected abstract ContentPanel createContentPanel(String id,
            IModel<T> model);
    
    public abstract void onSubmit(AjaxRequestTarget target, T object);

    public void onCancel(AjaxRequestTarget target, T object) {
    }

} // class ModalEditDialog
