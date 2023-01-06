package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;
import java.util.List;

public interface TimerManager extends Serializable {

    AbstractDefaultAjaxBehavior getTimerBehavior();

    /**
     * Add an ajax target to be updated on each timer event
     */
    void addTarget(AjaxRequestTarget target, Update update);

    /**
     * Add a callback which will be invoked on each timer event before updating the available targets.
     */
    void addCallback(TimerCallback callback);

    /**
     * Do work on timer events, controls when timer is stopped when all updates are done
     */
    public interface Update extends Serializable {
        boolean onUpdate(AjaxRequestTarget target);
        List<Component> getComponents();
    }

    /**
     * Callback triggered on timer updates
     */
    public interface TimerCallback extends Serializable {
        public void invokeCallback(AjaxRequestTarget target);
    }
}
