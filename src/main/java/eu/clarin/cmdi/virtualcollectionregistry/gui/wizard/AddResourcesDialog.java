package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ModalDialogBase;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.service.impl.ReferenceValidator;

@SuppressWarnings("serial")
public abstract class AddResourcesDialog extends ModalDialogBase {
    private static final class Data implements Serializable {
        private Resource.Type type;
        private String[] references;

        public Resource[] getResources() {
            Resource[] value = null;
            if ((type != null) && (references != null) &&
                    (references.length > 0)) {
                value = new Resource[references.length];
                int i = 0;
                for (String ref : references) {
                    value[i++] = new Resource(type, ref);
                }
            }
            return value;
        }
    } // class AddResourcesDialog.Data

    private static final class Content extends Panel {
        private final Form<Data> form;
        private final FeedbackPanel feedbackPanel;

        public Content(String id) {
            super(id);
            add(new AttributeAppender("class",
                    new Model<String>("editDialog addResourcesDialog"), " "));
            form = new Form<Data>("addResourcesForm",
                    new CompoundPropertyModel<>((Data)null));
            final DropDownChoice<Resource.Type> typeChoice =
                new DropDownChoice<Resource.Type>("type",
                        Arrays.asList(Resource.Type.values()),
                        new EnumChoiceRenderer<Resource.Type>(this));
            typeChoice.setRequired(true);
            form.add(typeChoice);
            final AbstractTextComponent<String[]> referencesArea =
                new AbstractTextComponent<String[]>("references") {
                    @Override
                    protected void convertInput() {
                        final String input = getRawInput();
                        if (input != null) {
                            final String[] refs = input.split("[,;\\s]+");
                            setConvertedInput(refs);
                        } else {
                            setConvertedInput(null);
                        }
                    }
                };
            referencesArea.setRequired(true);
            referencesArea.add(new AbstractValidator<String[]>() {
                @Override
                protected void onValidate(IValidatable<String[]> input) {
                    String[] refs = input.getValue();
                    if (refs != null) {
                        ReferenceValidator v = new ReferenceValidator();
                        for (String ref : refs) {
                            if (ref.length() > 255) {
                                ValidationError ve = new ValidationError();
                                ve.setMessage("'" + ref +
                                        "' is larger than 255 characters");
                                input.error(ve);
                                continue;
                            }
                            Validatable<String> w =
                                new Validatable<String>(ref);
                            v.validate(w);
                            if (!w.isValid()) {
                                ValidationError ve = new ValidationError();
                                ve.setMessage("'" + ref +
                                        "' is not valid uri");
                                input.error(ve);
                            }
                        }
                    }
                }
            });
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
        //TODO: wicket 1.5 upgrade, is this removed? See https://issues.apache.org/jira/browse/WICKET-2213
        //contentPanel.getForm().removePersistentFormComponentValues(true);
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
