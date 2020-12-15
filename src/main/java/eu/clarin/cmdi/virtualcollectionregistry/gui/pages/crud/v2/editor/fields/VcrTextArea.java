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
    public VcrTextArea(String id, String label, final IModel dataModel, VisabilityUpdater v) {
        this(id, label, null, dataModel, v);
    }

    public VcrTextArea(String id, String label, String placeHolderValue, final IModel dataModel, VisabilityUpdater v) {
        this(id, label, placeHolderValue, dataModel, v, true);
    }

    public VcrTextArea(String id, String label, String placeHolderValue, final IModel dataModel, VisabilityUpdater v, boolean markdownSupported) {
        super(id, label, dataModel, null, new TextArea("input", dataModel), false, v);

        Label lbl = new Label("markdown", "Note: Markdown supported (<a target=\"_new\" href=\"https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet\">cheat sheet</a>)");
        lbl.setEscapeModelStrings(false);
        lbl.setVisible(markdownSupported);
        add(lbl);

        if(placeHolderValue != null && !placeHolderValue.isEmpty()) {
            editComponent.add(new AttributeModifier("placeholder", placeHolderValue));
        }
    }
}
