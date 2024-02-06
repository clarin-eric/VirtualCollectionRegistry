package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.AbstractDialogEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

/**
 *
 * @author wilelb
 */
public class ModalConfirmAction <T> implements Serializable {
    private final EventType type;
    private final T data;

    public ModalConfirmAction(EventType type, T data) {
        this.type = type;
        this.data = data;
    }

    public Event getEvent(AjaxRequestTarget target, Modal modal) {
        return new AbstractDialogEvent<>(type, data, target, modal);
    }
}
