package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

/**
 *
 * @author wilelb
 */
public class VcrTextField extends AbstractField {
       
        public VcrTextField(String id, String label, String defaultValue, final IModel dataModel) {
            this(id, label, defaultValue, dataModel, null);
        }
        
        public VcrTextField(String id, String label, String defaultValue, final IModel dataModel, final FieldComposition parent) {
            super(id, label, defaultValue, dataModel, parent, new TextField("input_textfield", dataModel));
            if(defaultValue != null) {
                editComponent.add(new AttributeModifier("placeholder", defaultValue));
            }
        }
}
