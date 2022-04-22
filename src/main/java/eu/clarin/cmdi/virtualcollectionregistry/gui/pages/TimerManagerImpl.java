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

    public TimerManagerImpl() {
        timer = new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if(target != null) {
                    logger.trace("TimerManagerImpl onTimer, #targets="+targets.size());

                    List<Integer> idxToRemove = new ArrayList<>();
                    for(int i = 0; i < targets.size(); i++) {
                        for(Component c : targets.get(i).getComponents()) {
                            target.add(c);
                        }

                        if(!targets.get(i).onUpdate(target)) {
                            idxToRemove.add(i);
                        }
                    }

                    for(Integer idx : idxToRemove) {
                        removeTarget(targets.get(idx), target);
                    }
                }
            }
        };
        timer.stop(null);
    }

    public AbstractDefaultAjaxBehavior getTimerBehavior() {
        return timer;
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
        targets.remove(update);
        if(targets.isEmpty()) {
            logger.info("TimerManagerImpl stopping timer");
            timer.stop(target);
        }
    }
}
