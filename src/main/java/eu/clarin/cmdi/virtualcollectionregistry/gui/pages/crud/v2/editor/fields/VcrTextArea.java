package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author wilelb
 */
public class VcrTextArea extends AbstractField {
    public VcrTextArea(String id, String label, final IModel dataModel, VisabilityUpdater v) {
        this(id, label, null, null, dataModel, v);
    }

    public VcrTextArea(String id, String label, String placeHolderValue, final IModel dataModel, VisabilityUpdater v) {
        this(id, label, placeHolderValue, null, dataModel, v, true);
    }

    public VcrTextArea(String id, String label, String placeHolderValue, String help_text, final IModel dataModel, VisabilityUpdater v) {
        this(id, label, placeHolderValue, help_text, dataModel, v, true);
    }

    public VcrTextArea(String id, String label, String placeHolderValue, final IModel dataModel, VisabilityUpdater v, boolean markdownSupported) {
        this(id, label, placeHolderValue, null, dataModel, v, markdownSupported);
    }

    public VcrTextArea(String id, String label, String placeHolderValue, String help_text, final IModel dataModel, VisabilityUpdater v, boolean markdownSupported) {
        super(id, label, help_text, dataModel, null, new TextArea("input", dataModel), false, v);
/*
        WebMarkupContainer helpMessage = new WebMarkupContainer("help_message");
        helpMessage.add(new Label("message", Model.of(help_text == null ? "" : help_text)));
        helpMessage.setVisible(help_text != null);
        add(helpMessage);
        */
        Label lbl = new Label("markdown", "Note: Markdown supported (<a target=\"_new\" href=\"https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet\">cheat sheet</a>)");
        lbl.setEscapeModelStrings(false);
        lbl.setVisible(markdownSupported);
        add(lbl);

        if(placeHolderValue != null && !placeHolderValue.isEmpty()) {
            editComponent.add(new AttributeModifier("placeholder", placeHolderValue));
        }
    }
}
