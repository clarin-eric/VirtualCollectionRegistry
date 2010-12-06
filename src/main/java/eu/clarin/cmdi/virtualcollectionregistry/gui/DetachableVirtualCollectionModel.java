package eu.clarin.cmdi.virtualcollectionregistry.gui;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
public class DetachableVirtualCollectionModel implements
    IModel<VirtualCollection>, IDetachable {
    private long id;
    private transient VirtualCollection vc;

    public DetachableVirtualCollectionModel(VirtualCollection vc) {
        if (vc == null) {
            throw new IllegalArgumentException("vc == null");
        }
        this.id = vc.getId();
        this.vc = vc;
    }

    public DetachableVirtualCollectionModel(long id) {
        this.id = vc.getId();
    }

    @Override
    public VirtualCollection getObject() {
        if (!isAttached()) {
            this.vc = load();
        }
        return this.vc;
    }

    @Override
    public void setObject(VirtualCollection vc) {
        throw new UnsupportedOperationException("Model " + getClass() +
                " does not support setObject(Object)");
    }

    @Override
    public void detach() {
        if (isAttached()) {
            this.vc = null;
        }
    }

    public boolean isAttached() {
        return vc != null;
    }

    protected VirtualCollection load() {
        try {
            VirtualCollectionRegistry vcr =
                VirtualCollectionRegistry.instance();
            return vcr.retrieveVirtualCollection(this.id);
        } catch (VirtualCollectionRegistryException e) {
            return null;
        }
    }

} // class DetachableVirtualCollectionModel
