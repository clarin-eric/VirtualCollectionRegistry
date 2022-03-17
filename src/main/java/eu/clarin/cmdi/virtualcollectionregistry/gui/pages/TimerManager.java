package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;
import java.util.List;

public interface TimerManager {

    AbstractDefaultAjaxBehavior getTimerBehavior();
    void addTarget(AjaxRequestTarget target, Update update);

    public interface Update extends Serializable {
        boolean onUpdate(AjaxRequestTarget target);
        List<Component> getComponents();
    }

}
