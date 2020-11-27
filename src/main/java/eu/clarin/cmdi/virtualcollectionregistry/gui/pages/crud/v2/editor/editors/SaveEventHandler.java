package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors;

import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

/**
 *
 * @author wilelb
 */
public interface SaveEventHandler extends Serializable {
     public void handleSaveEvent(AjaxRequestTarget target);
}
