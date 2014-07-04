package eu.clarin.cmdi.virtualcollectionregistry.gui;

import javax.persistence.EntityManager;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model for volatile entity objects: objects that are managed by a persistence
 * context but do not get persisted before the end of the request cycle and
 * therefore lose their connection to the context. This model merges the model
 * object with the persistence context on the first call of {@link #getObject()
 * } after {@link #detach() }.
 *
 * @author twagoo
 * @see EntityManager#merge(java.lang.Object)
 */
public class VolatileEntityModel<T> implements IModel<T> {

    private final static Logger logger = LoggerFactory.getLogger(VolatileEntityModel.class);
    private T object;
    private boolean attached;

    public VolatileEntityModel(T object) {
        this.object = object;
        attached = true;
    }

    /**
     *
     * @return the previously stored object or a merged copy of it if the model
     * was detached
     */
    @Override
    public T getObject() {
        if (!attached) {
            attach();
        }
        return object;
    }

    private void attach() {
        logger.trace("Merging volatile object ({}) into persistence context", object);
        final EntityManager em = Application.get().getDataStore().getEntityManager();
        attached = true;
        object = em.merge(object);
    }

    @Override
    public void setObject(T object) {
        this.object = object;
    }

    @Override
    public void detach() {
        logger.trace("Detaching volatile object ({})", object);
        // set flag so that on next call to getObject() object will be merged
        attached = false;
    }

}
