package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.security.Principal;

public class DataUpdatedEvent implements Event {

    private final  AjaxRequestTarget target;

    public DataUpdatedEvent(AjaxRequestTarget target) {
        this.target = target;
    }

    @Override
    public EventType getType() {
        return null;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public AjaxRequestTarget getAjaxRequestTarget() {
        return target;
    }

    @Override
    public void updateTarget() {

    }

    @Override
    public void updateTarget(Component c) {

    }

    @Override
    public Principal getPrincipal() {
        return null; //Not used
    }
}

