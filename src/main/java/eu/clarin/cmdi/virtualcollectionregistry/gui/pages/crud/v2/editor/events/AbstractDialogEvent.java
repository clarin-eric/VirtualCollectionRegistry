package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author wilelb
 */
public class AbstractDialogEvent<T> extends AbstractEvent<T> {
    private final Modal modal;
    
    public AbstractDialogEvent(EventType type, AjaxRequestTarget target, Modal modal) {
        this(type, null, target, modal);
    }
    
    public AbstractDialogEvent(EventType type, T data, AjaxRequestTarget target, Modal modal) {
        super(type, data, target);
        this.modal = modal;
    }
    
    @Override
    public void updateTarget(Component c) {
        if(this.target != null) {
            super.updateTarget(c);
            this.modal.close(target);        
        }
    }
    
}
