package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;

@SuppressWarnings("serial")
public abstract class AddResourcesDialog extends ModalWindow {
    private final class Content extends Panel {
        private Resource.Type type;
        private String refernces;

        public Content(String id, final ModalWindow window) {
            super(id);
            final Form<Object> form =
                new Form<Object>("addResourcesForm");
            final DropDownChoice<Resource.Type> typeChoice =
                new DropDownChoice<Resource.Type>("type",
                        new PropertyModel<Resource.Type>(this, "type"),
                        Arrays.asList(Resource.Type.values()),
                        new EnumChoiceRenderer<Resource.Type>(this));
            typeChoice.setRequired(true);
            form.add(typeChoice);
            final TextArea<String> referencesArea =
                new TextArea<String>("references", new PropertyModel<String>(this, "refernces"));
            referencesArea.setRequired(true);
            form.add(referencesArea);
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
                    if ((refernces != null) && !refernces.isEmpty()) {
                        // FIXME: split pattern?
                        String[] refs = refernces.split("\n");
                        if ((type != null) && (refs != null) && (refs.length > 0)) {
                            Resource[] resources = new Resource[refs.length];
                            int i = 0;
                            for (String ref : refs) {
                                resources[i++] = new Resource(type, ref);
                            }
                            AddResourcesDialog.this.onSubmit(target, resources);
                        }
                    }
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
    } // class AddResourcesDialog.Content

    private final Content content;


    public AddResourcesDialog(final String id) {
        super(id);
        setOutputMarkupId(true);
        setTitle(new Model<String>("Add More Resources"));
        content = new Content(getContentId(), AddResourcesDialog.this);
        content.setOutputMarkupId(true);
        setContent(content);
        setInitialWidth(350);
        setInitialHeight(200);
    }

    public abstract void onSubmit(AjaxRequestTarget target, Resource[] resources);

} // AddResourcesDialog
