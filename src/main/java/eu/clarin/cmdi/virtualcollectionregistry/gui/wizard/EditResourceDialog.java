package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;

@SuppressWarnings("serial")
public abstract class EditResourceDialog extends ModalWindow {
    private final class Content extends Panel {
        private final Form<Resource> form;

        public Content(String id, final ModalWindow window) {
            super(id);
            form = new Form<Resource>("editResourceForm",
                    new CompoundPropertyModel<Resource>(new Resource()));
            final DropDownChoice<Resource.Type> typeChoice =
                new DropDownChoice<Resource.Type>("type",
                        Arrays.asList(Resource.Type.values()),
                        new EnumChoiceRenderer<Resource.Type>(this));
            typeChoice.setRequired(true);
            form.add(typeChoice);
            form.add(new RequiredTextField<String>("ref"));
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
                    EditResourceDialog.this.onSubmit(target,
                            (Resource) form.getModelObject());
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
        
        private void updateModel(Resource resource) {
            form.setModel(new CompoundPropertyModel<Resource>(resource));
        }
    } // class EditResourceDialog.Content

    private final Content content;


    public EditResourceDialog(final String id) {
        super(id);
        setOutputMarkupId(true);
        setTitle(new Model<String>("New Resource"));
        content = new Content(getContentId(), EditResourceDialog.this);
        content.setOutputMarkupId(true);
        setContent(content);
        setInitialWidth(350);
        setUseInitialHeight(false);
    }

    public void setResource(Resource resource) {
        if (resource != null) {
            content.updateModel(resource);
        }
    }

    public abstract void onSubmit(AjaxRequestTarget target, Resource Resource);

} // EditResourceDialog
