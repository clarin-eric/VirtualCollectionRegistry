package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TimerManagerImpl implements TimerManager, Serializable {

    private final static Logger logger = LoggerFactory.getLogger(TimerManagerImpl.class);

    private final AbstractAjaxTimerBehavior timer;

    private final List<Update> targets = new ArrayList<>();

    private final List<TimerCallback> callbacks = new ArrayList<>();

    public TimerManagerImpl() {
        List<Integer> idxToRemove = new ArrayList<>();
        timer = new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                //Run all callbacks on this timer update
                for(TimerCallback callback : callbacks) {
                    callback.invokeCallback(target);
                }

                //Update all targets for this timer update
                if(target != null) {
                    logger.trace("TimerManagerImpl onTimer, #targets="+targets.size());

                    //Process remove list and remove update targets, if all update targets are removed, the timer is stopped
                    for(Integer idx : idxToRemove) {
                        TimerManager.Update update = null;
                        if(targets.size()>idx) {
                            update=targets.get(idx);
                        }
                        removeTarget(update, target);
                    }

                    for(int i = 0; i < targets.size(); i++) {
                        //Add component to ajax update target
                        for(Component c : targets.get(i).getComponents()) {
                            target.add(c);
                        }

                        //Add update target to remove list if updates are no longer needed. These should be processed in the next timer update
                        //to allow the ui to update with any pending changes
                        if(!targets.get(i).onUpdate(target)) {
                            idxToRemove.add(i);
                        }
                    }
                }
            }
        };
        timer.stop(null);
    }

    public AbstractDefaultAjaxBehavior getTimerBehavior() {
        return timer;
    }

    public void addCallback(TimerCallback callback) {
        callbacks.add(callback);
    }

    public synchronized void addTarget(AjaxRequestTarget target, TimerManager.Update update) {
        logger.info("TimerManagerImpl added target");
        targets.add(update);
        if(!targets.isEmpty()) {
            logger.info("TimerManagerImpl starting timer");
            timer.restart(target);
        }
    }

    public void removeTarget(TimerManager.Update update, AjaxRequestTarget target) {
        if(update != null) {
            targets.remove(update);
        }
        if(targets.isEmpty()) {
            logger.info("TimerManagerImpl stopping timer");
            timer.stop(target);
        }
    }
}
