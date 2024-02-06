package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author wilelb
 */
public interface EventHandler<T> {
    public void handleEditEvent(T object, AjaxRequestTarget target);
    public void handleRemoveEvent(T object, AjaxRequestTarget target);
}
