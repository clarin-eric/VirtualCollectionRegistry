
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class VcrChoiceField extends AbstractField {
    private static final Logger logger = LoggerFactory.getLogger(VcrChoiceField.class);

    public VcrChoiceField(String id, String label, List<String> choices, final IModel<String> dataModel, VisabilityUpdater v) {
        this(id, label, choices, null, dataModel, v);
    }

    public VcrChoiceField(String id, String label, List<String> choices, String help_text, final IModel<String> dataModel, VisabilityUpdater v) {
        super(id, label, help_text, dataModel, null, new RadioChoice<>("input_radio_choice", dataModel, choices), false, v);
        /*
        WebMarkupContainer helpMessage = new WebMarkupContainer("help_message");
        Label helpMessageLabel = new Label("message", Model.of(help_text == null ? "" : help_text));
        helpMessageLabel.setEscapeModelStrings(false);
        helpMessage.add(helpMessageLabel);
        helpMessage.setVisible(help_text != null);
        add(helpMessage);*/
    }
        
    @Override
    protected AjaxFormChoiceComponentUpdatingBehavior getOnBlurUpdatingBehavior(final FieldComposition parent, final Component t) {
        return new AjaxFormChoiceComponentUpdatingBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                logger.trace("onUpdate: triggered via onBlur");
                if(validate()) {
                    handleUpdateData(target, dataModel, nextComponentToFocus);                    
                    if(parent != null) {
                        parent.completeSubmit(target);
                    }
                }
                target.add(t);
            }
        };
    }
}
