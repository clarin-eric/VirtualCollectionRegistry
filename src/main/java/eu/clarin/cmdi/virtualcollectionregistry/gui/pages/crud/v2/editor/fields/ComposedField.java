package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author wilelb
 */
public abstract class ComposedField extends AbstractField implements FieldComposition {
    
    public ComposedField(String id, String label, Component editComponent, VisabilityUpdater v) {
        super(id, label, null, editComponent, v);
    }
 
    @Override
    public void increaseFocusCount() {
    }

    @Override
    public void decreaseFocusCount() {
    }
    
    @Override
    public abstract boolean completeSubmit(AjaxRequestTarget target);
}
