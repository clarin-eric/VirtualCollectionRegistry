package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

public class VcrTextFieldWithoutLabel extends AbstractField {

    public VcrTextFieldWithoutLabel(String id, final IModel dataModel, VisabilityUpdater v) {
        this(id, null, dataModel, null, v);
    }

    public VcrTextFieldWithoutLabel(String id, String placeHolderValue, final IModel dataModel, final FieldComposition parent, VisabilityUpdater v) {
        super(id, null, dataModel, parent, new TextField("input_textfield", dataModel), true, v);
        if(placeHolderValue != null && !placeHolderValue.isEmpty()) {
            editComponent.add(new AttributeModifier("placeholder", placeHolderValue));
        }
    }
}
