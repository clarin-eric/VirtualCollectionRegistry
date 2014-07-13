package eu.clarin.cmdi.virtualcollectionregistry.gui;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
public class DetachableVirtualCollectionModel
        extends LoadableDetachableModel<VirtualCollection> {

    private long id;

    public DetachableVirtualCollectionModel(VirtualCollection vc) {
        super(vc);
        if (vc == null) {
            throw new IllegalArgumentException("vc == null");
        }
        this.id = vc.getId();
    }

    public DetachableVirtualCollectionModel(long id) {
        this.id = id;
    }

    @Override
    protected VirtualCollection load() {
        try {
            VirtualCollectionRegistry vcr
                    = Application.get().getRegistry();
            return vcr.retrieveVirtualCollection(this.id);
        } catch (VirtualCollectionRegistryException e) {
            return null;
        }
    }

} // class DetachableVirtualCollectionModel
