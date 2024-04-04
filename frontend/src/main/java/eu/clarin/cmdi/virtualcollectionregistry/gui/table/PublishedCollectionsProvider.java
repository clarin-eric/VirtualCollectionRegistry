package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryOptions;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class PublishedCollectionsProvider extends CollectionsProvider {

    @Override
    protected void addSpaceFilter(QueryFactory factory) {
        factory.and(QueryOptions.Property.VC_PUBLIC_LEAF, QueryOptions.Relation.EQ, Boolean.TRUE);
        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC);
        states.add(VirtualCollection.State.PUBLIC_FROZEN);
        factory.and(QueryOptions.Property.VC_STATE, QueryOptions.Relation.IN, states);
    }

}
