package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ModalDialogBase;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;

@SuppressWarnings("serial")
public abstract class AddResourcesDialog extends ModalDialogBase {
    private final class Data implements Serializable {
        private Resource.Type type;
        private String references;
        
        public Resource[] getResources() {
            Resource[] resources = null;
            if ((type != null) && (references != null)) {
                String[] refs = references.split("[;\\s]+");
                if (refs.length > 0) {
                    resources = new Resource[refs.length];
                    int i = 0;
                    for (String ref : refs) {
                        resources[i++] = new Resource(type, ref);
                    }
                }
            }
            return resources;
        }
    } // class AddResourcesDialog.Data

    private final class Content extends Panel {
        private final Form<Data> form;
        private final FeedbackPanel feedbackPanel;

        public Content(String id) {
            super(id);
            add(new AttributeAppender("class",
                    new Model<String>("editDialog addResourcesDialog"), " "));
            form = new Form<Data>("addResourcesForm",
                    new CompoundPropertyModel<Data>(null));
            final DropDownChoice<Resource.Type> typeChoice =
                new DropDownChoice<Resource.Type>("type",
                        Arrays.asList(Resource.Type.values()),
                        new EnumChoiceRenderer<Resource.Type>(this));
            typeChoice.setRequired(true);
            form.add(typeChoice);
            final TextArea<String> referencesArea =
                new TextArea<String>("references");
            referencesArea.setRequired(true);
            form.add(referencesArea);
            feedbackPanel = new FeedbackPanel("feedback");
            feedbackPanel.setOutputMarkupId(true);
            form.add(feedbackPanel);
            add(form);
        }

        public Form<Data> getForm() {
            return form;
        }
        
        public FeedbackPanel getFeedbackPanel() {
            return feedbackPanel;
        }
    } // class AddResourcesDialog.Content

    private final class ButtonBar extends Panel {
        public ButtonBar(String id, Form<?> form) {
            super(id);
            final AjaxButton addButton = new AjaxButton("addButton",
                    new Model<String>("Add"), form) {
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(contentPanel.getFeedbackPanel());
                    super.onError(target, form);
                }

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    AddResourcesDialog.this.close(target);
                    final Data data = (Data) form.getModelObject();
                    final Resource[] resources = data.getResources();
                    if (resources != null) {
                        AddResourcesDialog.this.onSubmit(target, resources);
                    }
                    form.setModelObject(null);
                }
            };
            add(addButton);
            final AjaxButton cancelButton =
                new AjaxButton("cancelButton",
                        new Model<String>("Cancel"), form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    AddResourcesDialog.this.close(target);
                    form.setModelObject(null);
                }
            };
            cancelButton.setDefaultFormProcessing(false);
            add(cancelButton);
        }
    } // class EditCreatorDialog.ButtonBar

    private Content contentPanel;

    public AddResourcesDialog(final String id) {
        super(id, new Model<String>("Add Multiple Resources"));
        setOutputMarkupId(true);
        setInitialWidth(600);
    }

    @Override
    protected Panel createContent(String id) {
        contentPanel = new Content(id);
        contentPanel.getForm().removePersistentFormComponentValues(true);
        return contentPanel;
    }

    @Override
    protected Panel createButtonBar(String id) {
        return new ButtonBar(id, contentPanel.getForm());
    }

    @Override
    public void show(AjaxRequestTarget target) {
        contentPanel.getForm().setModelObject(new Data());
        super.show(target);
    }

    public abstract void onSubmit(AjaxRequestTarget target,
            Resource[] resources);

} // AddResourcesDialog
