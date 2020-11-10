package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author wilelb
 */
public interface FieldComposition {
    public boolean completeSubmit(AjaxRequestTarget target);
    public void increaseFocusCount();
    public void decreaseFocusCount();
}
