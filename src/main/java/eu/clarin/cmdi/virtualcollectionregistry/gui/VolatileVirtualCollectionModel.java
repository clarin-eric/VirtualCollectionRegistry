package eu.clarin.cmdi.virtualcollectionregistry.gui;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import javax.persistence.EntityManager;
import org.apache.wicket.model.LoadableDetachableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class VolatileVirtualCollectionModel extends LoadableDetachableModel<VirtualCollection> {

    private final static Logger logger = LoggerFactory.getLogger(VolatileVirtualCollectionModel.class);
    private VirtualCollection collection;

    public VolatileVirtualCollectionModel(VirtualCollection collection) {
        super(collection);
        this.collection = collection;
    }

    @Override
    protected VirtualCollection load() {
        final EntityManager em = Application.get().getDataStore().getEntityManager();
        logger.debug("Merging volatile virtual collection (id={}) into persistence context", collection.getId());
        return (collection = em.merge(collection));
    }

}
