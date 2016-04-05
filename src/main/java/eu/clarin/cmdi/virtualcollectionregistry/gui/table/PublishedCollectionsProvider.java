package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class PublishedCollectionsProvider extends CollectionsProvider {

    @Override
    protected void addSpaceFilter(QueryOptions.Filter filter) {
        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC);
        states.add(VirtualCollection.State.PUBLIC_FROZEN);
        filter.add(QueryOptions.Property.VC_STATE,
                QueryOptions.Relation.IN,
                states);
    }

}
