package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.CollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PrivateCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.WicketRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.Principal;
import java.util.Date;
import java.util.List;

public class PrivateCollectionsManager implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(PrivateCollectionsManager.class);

    private final CollectionsProvider provider;
    private final transient Principal principal;

    public PrivateCollectionsManager(Principal principal) {
        this.provider = new PrivateCollectionsProvider();
        this.principal = principal;
    }

    public VirtualCollection remove(int i) {
        logger.info("Removing collection not implemented");
        return this.provider.getList().remove(i);
    }

    public VirtualCollection get(int i) {
        return this.provider.getList().get(i);
    }

    public void set(int i, VirtualCollection collection) throws VirtualCollectionRegistryException {
        logger.debug("Updating existing virtual collection with id: {}", collection.getId());
        Application.get().getRegistry().updateVirtualCollection(principal, collection.getId(), collection);
        this.provider.getList().set(i, collection);
    }

    public void add(VirtualCollection collection) throws VirtualCollectionRegistryException {
        logger.debug("Creating new virtual collection");
        Application.get().getRegistry().createVirtualCollection(principal, collection);
        this.provider.getList().add(collection);
    }

    public int size() {
        return this.provider.getList().size();
    }

    public boolean isEmpty() {
        return this.provider.getList().isEmpty();
    }

    public List<VirtualCollection> getList() {
        return this.provider.getList();
    }
}
