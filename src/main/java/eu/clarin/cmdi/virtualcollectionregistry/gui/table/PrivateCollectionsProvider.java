package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class PrivateCollectionsProvider extends CollectionsProvider {

    @Override
    protected void addSpaceFilter(QueryFactory factory) {
        ApplicationSession session = ApplicationSession.get();
        factory.and(QueryOptions.Property.VC_OWNER, QueryOptions.Relation.EQ, session.getUser());

        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC);
        states.add(VirtualCollection.State.PUBLIC_PENDING);
        states.add(VirtualCollection.State.PUBLIC_FROZEN);
        states.add(VirtualCollection.State.PUBLIC_FROZEN_PENDING);
        states.add(VirtualCollection.State.PRIVATE);
        factory.and(QueryOptions.Property.VC_STATE, QueryOptions.Relation.IN, states);
    }
/*
    protected void addTypeFilter(QueryOptions.Filter filter, VirtualCollection.Type type) {
        filter.add(QueryOptions.Property.VC_TYPE,
                QueryOptions.Relation.EQ,
                type);
    }
*/
}
