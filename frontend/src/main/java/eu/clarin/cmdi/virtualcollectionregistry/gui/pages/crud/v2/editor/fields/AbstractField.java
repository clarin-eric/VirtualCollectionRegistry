package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.AjaxFormComponentOnKeySubmitBehavior;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.DataUpdatedEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Editable TextField with label. 
 * Input is updated into the model onBlur and onPress (enter)
 * 
 * @author wilelb
 */
public abstract class AbstractField extends Panel implements Field {
    private static final Logger logger = LoggerFactory.getLogger(AbstractField.class);
    
    //Clear TextField on submit, defaults to false;
    private boolean resetOnSubmit = false;
    
    //If set to true, this component triggers a complete call on the parent when
    //losing focus (typically the last component of  form)
    //private boolean triggerComplete = false;
    
    //The next component to receive focus after submitting this component (can be null)
    protected Component nextComponentToFocus = null;
    
    protected Component componentToTakeFocus = null;
    
    private final List<InputValidator> inputValidators = new ArrayList<>();
    
    protected boolean required = false;
    
    private final Model errorMessageModel = Model.of("");
    private Label lblErrorMessage;
    
    protected final IModel dataModel;
    
    protected final Component editComponent;
    
    private boolean completeSubmitOnUpdate = false;
    
    private final IModel labelModel;
    private String label;
    private Label lbl;

    private final boolean enableOnKeySubmit;

    private final VisabilityUpdater visabilityUpdater;

    private final WebMarkupContainer helpMessage;
    private final String helpText;

    public AbstractField(String id, String label, String help_text, Component editComponent, VisabilityUpdater visabilityUpdater) {
        this(id, label, help_text, new Model<>(), null, editComponent, true, visabilityUpdater);
    }
    
    public AbstractField(String id, String label, String help_text, final IModel dataModel, final FieldComposition parent, Component editComponent, boolean enableOnKeySubmit, VisabilityUpdater visabilityUpdater) {
        super(id);
        this.visabilityUpdater = visabilityUpdater;
        this.label = label;
        this.editComponent = editComponent;
        this.dataModel = dataModel;
        this.enableOnKeySubmit = enableOnKeySubmit;
        setOutputMarkupId(true);

        addUpdatingBehavior(editComponent, parent, this);
        if(editComponent != null) {
            add(editComponent);
            componentToTakeFocus = editComponent;
        }

        if(label != null) {
            labelModel = Model.of(label.isEmpty() ? "" : label + ":");
            lbl = new Label("label", labelModel);
            add(lbl);
        } else {
            labelModel = Model.of("");
        }

        lblErrorMessage = new Label("error_message", errorMessageModel);
        lblErrorMessage.setEscapeModelStrings(false);
        lblErrorMessage.setVisible(false);
        add(lblErrorMessage);

        helpMessage = new WebMarkupContainer("help_message");
        WebMarkupContainer helpMessageIcon = new WebMarkupContainer("icon");
        helpMessage.add(helpMessageIcon);
        this.helpText = help_text;
        Label helpMessageLabel = new Label("message", Model.of(help_text == null ? "" : help_text));
        helpMessageLabel.setEscapeModelStrings(false);
        helpMessage.add(helpMessageLabel);
        helpMessage.setVisible(help_text != null);
        add(helpMessage);
        helpMessage.setVisible(false);
    }

    public void showHelp(boolean showHelp) {
        helpMessage.setVisible(this.helpText != null && !this.helpText.isEmpty() && showHelp);
    }

    public void updateVisability() {
        if(this.visabilityUpdater != null) {
            this.visabilityUpdater.updateVisability(this);
        }
    }
    
    protected void addUpdatingBehavior(Component c, final FieldComposition parent, final Component t) {
        if(c != null) {
            //c.add(getOnFocusUpdatingBehavior(parent));
            c.add(getOnBlurUpdatingBehavior(parent, t));
            if(this.enableOnKeySubmit) {
                c.add(getOnKeySubmitBehavior(parent, t));
            }
        }
    }
    
    protected AjaxFormComponentUpdatingBehavior getOnFocusUpdatingBehavior(final FieldComposition parent) {
        return new AjaxFormComponentUpdatingBehavior("focus") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(parent != null) {
                    parent.increaseFocusCount();
                }
            }
        };
    }
    
    protected AjaxFormComponentUpdatingBehavior getOnBlurUpdatingBehavior(final FieldComposition parent, final Component t) {
        return new AjaxFormComponentUpdatingBehavior("blur") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                logger.trace("onUpdate: triggered via onBlur");
                if(validate()) {
                    handleUpdateData(target, dataModel, nextComponentToFocus);                    
                    if(parent != null && completeSubmitOnUpdate) {
                        parent.decreaseFocusCount();
                        parent.completeSubmit(target);
                    }
                }

                target.add(t);
            }
        };
    }
    
    protected AjaxFormComponentOnKeySubmitBehavior getOnKeySubmitBehavior(final FieldComposition parent, final Component t) {
        return new AjaxFormComponentOnKeySubmitBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {               
                if(pressedReturn()) {
                    logger.trace("onUpdate: pressedReturn() == true");  
                    if(validate()) {
                        handleUpdateData(target, dataModel, nextComponentToFocus);                        
                        if(parent != null) {
                            parent.completeSubmit(target);
                        }
                    }
                    target.add(t);
                }
            }
        };
    }
    
    public boolean validate() {
        String input = (String)dataModel.getObject(); 
        
        //Check for value if required == true
        if(required && (input == null || input.isEmpty())) {
            return setError("Required field.");
        }

        //If any validator fails, set error message and return false
        if(input != null) {
            for(InputValidator v : this.inputValidators) {
                if(!v.validate(input)) {
                    return setError(v.getErrorMessage());
                }
            }
        }
        
        //All validators passed, reset error message and return true
        return setError(null);
    }
    
    public void addValidator(InputValidator validator) {
        if(validator != null) {
            this.inputValidators.add(validator);
        }
    }
    
    public void setRequired(boolean required) {
        this.required = required;
        if(lbl != null) {
            labelModel.setObject(label.isEmpty() ? "" : label + ":");
            if(required) {
                lbl.add(new AttributeModifier("class", "required"));
            } else {
                lbl.add(new AttributeModifier("class", "optional"));
            }
        }
    }
    
    public Component getComponentToTakeFocus() {
        return componentToTakeFocus;
    }
    
    public void setNextComponentToFocus(AbstractField f) {
        if(f != null) {
            this.nextComponentToFocus = f.getComponentToTakeFocus();
        }
    }
    
    public void setNextComponentToFocus(Component c) {
        this.nextComponentToFocus = c;
    }
    
    public void setResetOnSubmit(boolean resetOnSubmit) {
        this.resetOnSubmit = resetOnSubmit;
    }
    
    //@Override
    protected void handleUpdateData(AjaxRequestTarget target, IModel modelToUpdate, Component nextComponentToFocus) {        
        String value = (String) modelToUpdate.getObject();    
        if(resetOnSubmit) {
            modelToUpdate.setObject("");
        }

        fireEvent(new DataUpdatedEvent(target));

        if(target != null) {
            target.add(this);
            if(nextComponentToFocus != null) {
                target.focusComponent(nextComponentToFocus);
            }
        }
    }

    /**
     * @param completeSubmitOnUpdate the completeSubmitOnUpdate to set
     */
    public void setCompleteSubmitOnUpdate(boolean completeSubmitOnUpdate) {
        this.completeSubmitOnUpdate = completeSubmitOnUpdate;
    }

    protected boolean setError(String message) {
        if(message != null) {
            lblErrorMessage.setVisible(true);
            errorMessageModel.setObject(message);
            logger.debug("Validation failed for field with label = {}, message = {}", label, message);
            return false;
        }

        lblErrorMessage.setVisible(false);
        errorMessageModel.setObject("");
        return true;
    }

    private final List<Listener> actionListeners = new ArrayList<>();

    public void addListener(Listener l) {
        actionListeners.add(l);
    }

    public void removeListener(Listener l) {
        throw new RuntimeException("Not implemented");
    }

    protected void fireEvent(Event evt) {
        for(Listener l : actionListeners) {
            l.handleEvent(evt);
        }
    }
}
