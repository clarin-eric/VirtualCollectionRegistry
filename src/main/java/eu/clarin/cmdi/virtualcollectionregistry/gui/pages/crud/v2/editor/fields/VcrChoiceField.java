
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class VcrChoiceField extends AbstractField {
    private static final Logger logger = LoggerFactory.getLogger(VcrChoiceField.class);
    
    public VcrChoiceField(String id, String label, List<String> choices) {
        this(id, label, choices, null, Model.of(""), null);
    }
        
    public VcrChoiceField(String id, String label, List<String> choices, String defaultValue, final IModel dataModel, final FieldComposition parent) {
        super(id, label, defaultValue, dataModel, parent, new RadioChoice<>("input_radio_choice", dataModel, choices));
    }
        
    @Override
    protected AjaxFormComponentUpdatingBehavior getOnBlurUpdatingBehavior(final FieldComposition parent, final Component t) {
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
