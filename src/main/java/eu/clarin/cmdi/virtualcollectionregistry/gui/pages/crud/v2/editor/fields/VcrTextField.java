package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author wilelb
 */
public class VcrTextField extends AbstractField {
        public VcrTextField(String id, String label, String placeHolderValue, final IModel dataModel, VisabilityUpdater v) {
            this(id, label, placeHolderValue, null, dataModel, v);
        }
        public VcrTextField(String id, String label, String placeHolderValue, String help_text, final IModel dataModel, VisabilityUpdater v) {
            this(id, label, placeHolderValue, help_text, dataModel, null, v);
        }

        public VcrTextField(String id, String label, String placeHolderValue, final IModel dataModel, final FieldComposition parent, VisabilityUpdater v) {
            this(id, label, placeHolderValue, null, dataModel, parent, v);
        }

        public VcrTextField(String id, String label, String placeHolderValue, String help_text, final IModel dataModel, final FieldComposition parent, VisabilityUpdater v) {
            super(id, label, help_text, dataModel, parent, new TextField("input_textfield", dataModel), true, v);
/*
            WebMarkupContainer helpMessage = new WebMarkupContainer("help_message");
            helpMessage.add(new Label("message", Model.of(help_text == null ? "" : help_text)));
            helpMessage.setVisible(help_text != null);
            add(helpMessage);
*/
            if(placeHolderValue != null && !placeHolderValue.isEmpty()) {
                editComponent.add(new AttributeModifier("placeholder", placeHolderValue));
            }
        }
}
