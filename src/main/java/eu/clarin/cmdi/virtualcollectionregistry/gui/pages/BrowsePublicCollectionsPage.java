package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.util.Iterator;

import org.apache.wicket.WicketRuntimeException;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;

public class BrowsePublicCollectionsPage extends BasePage {

    @SuppressWarnings("serial")
    public BrowsePublicCollectionsPage() {
        super();
        final VirtualCollectionTable table =
            new VirtualCollectionTable("collectionsTable", false) {

            @Override
            protected int getCollectionsCount() {
                try {
                    final VirtualCollectionRegistry vcr =
                        VirtualCollectionRegistry.instance();
                    VirtualCollectionList results =
                        vcr.getVirtualCollections(null, -1, 0);
                    return results.getTotalCount();
                } catch (VirtualCollectionRegistryException e) {
                    throw new WicketRuntimeException(e);
                }
            }

            @Override
            protected Iterator<VirtualCollection> getCollections(int first,
                    int count) {
                try {
                    final VirtualCollectionRegistry vcr =
                        VirtualCollectionRegistry.instance();
                    VirtualCollectionList results =
                        vcr.getVirtualCollections(null, first, count);
                    return results.getItems().iterator();
                } catch (VirtualCollectionRegistryException e) {
                    throw new WicketRuntimeException(e);
                }
            }
        };
        add(table);
    }

} // class BrowsePublicCollectionsPage
