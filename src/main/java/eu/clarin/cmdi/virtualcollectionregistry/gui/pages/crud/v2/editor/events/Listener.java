package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events;

import java.io.Serializable;

/**
 *
 * @author wilelb
 */
public interface Listener<T> extends Serializable {
    public void handleEvent(Event<T> event);
}
