package eu.clarin.cmdi.virtualcollectionregistry.gui;

import javax.persistence.EntityManager;
import org.apache.wicket.model.LoadableDetachableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class LoadableDetachableVolatileEntityModel<T> extends LoadableDetachableModel<T> {

    private final static Logger logger = LoggerFactory.getLogger(LoadableDetachableVolatileEntityModel.class);
    private T object;

    public LoadableDetachableVolatileEntityModel(T object) {
        super(object);
        this.object = object;
    }

    @Override
    protected T load() {
        logger.debug("Merging volatile object ({}) into persistence context", object);
        final EntityManager em = Application.get().getDataStore().getEntityManager();
        return (object = em.merge(object));
    }

    @Override
    protected void onDetach() {
        logger.debug("Detaching volatile object ({})", object);
        super.onDetach();
    }

}
