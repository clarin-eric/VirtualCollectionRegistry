package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.security.Principal;

/**
 *
 * @author wilelb
 */
public interface Event<T> {
    public EventType getType();
    public T getData();
    public Principal getPrincipal();
    public AjaxRequestTarget getAjaxRequestTarget();
    public void updateTarget();
    public void updateTarget(Component c);
}
