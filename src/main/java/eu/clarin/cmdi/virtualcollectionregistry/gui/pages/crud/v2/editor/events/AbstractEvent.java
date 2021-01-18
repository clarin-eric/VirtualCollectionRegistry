package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.security.Principal;

/**
 *
 * @author wilelb
 * @param <T>
 */
public class AbstractEvent<T> implements Event<T> {

    private final EventType type;
    private final T data;
    protected final AjaxRequestTarget target;
    private final Principal principal;

    public AbstractEvent(EventType type, AjaxRequestTarget target) {
        this(type, null, target);
    }

    public AbstractEvent(EventType type, T data, AjaxRequestTarget target) {
        this(type, null, data, target);
    }

    public AbstractEvent(EventType type, Principal p, T data, AjaxRequestTarget target) {
        this.type = type;
        this.data = data;
        this.target = target;
        this.principal = p;
    }
    
    @Override
    public EventType getType() {
        return this.type;
    }

    @Override
    public T getData() {
        return this.data;
    }

    @Override
    public AjaxRequestTarget getAjaxRequestTarget() {
        return this.target;
    }
    
    @Override
    public void updateTarget() {
        this.updateTarget(null);
    }
    
    @Override
    public void updateTarget(Component c) {
        if(this.target != null && c != null) {
            this.target.add(c);
        }
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }
}
