package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

/**
 *
 * @author wilelb
 */
public class VcrTextArea extends AbstractField {
    public VcrTextArea(String id, String label, final IModel dataModel) {
        this(id, label, null, dataModel);
    }

    public VcrTextArea(String id, String label, String placeHolderValue, final IModel dataModel) {
        super(id, label, dataModel, null, new TextArea("input", dataModel), false);

        Label lbl = new Label("markdown", "Note: Markdown supported (<a target=\"_new\" href=\"https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet\">cheat sheet</a>)");
        lbl.setEscapeModelStrings(false);
        add(lbl);

        if(placeHolderValue != null && !placeHolderValue.isEmpty()) {
            editComponent.add(new AttributeModifier("placeholder", placeHolderValue));
        }
    }
}
