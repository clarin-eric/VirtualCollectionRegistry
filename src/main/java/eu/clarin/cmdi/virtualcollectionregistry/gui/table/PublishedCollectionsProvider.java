package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class PublishedCollectionsProvider extends CollectionsProvider {

    @Override
    protected void addSpaceFilter(QueryOptions.Filter filter) {
        filter.add(QueryOptions.Property.VC_STATE,
                QueryOptions.Relation.EQ,
                VirtualCollection.State.PUBLIC);
    }

}
