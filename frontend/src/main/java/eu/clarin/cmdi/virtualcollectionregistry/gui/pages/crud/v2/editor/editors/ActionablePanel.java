package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wilelb
 */
public abstract class ActionablePanel extends Panel {
    private final List<Listener> actionListeners = new ArrayList<>();
    
    public ActionablePanel(String id) {
        super(id);
    }
    
    public void addListener(Listener l) {
        actionListeners.add(l);
    }
    
    public void removeListener(Listener l) {
        throw new RuntimeException("Not implemented");
    }
    
    protected void fireEvent(Event evt) {
        for(Listener l : actionListeners) {
            l.handleEvent(evt);
        }
    }
}
