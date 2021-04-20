package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class PrivateCollectionsProvider extends CollectionsProvider {

    @Override
    protected void addSpaceFilter(QueryOptions.Filter filter) {
        ApplicationSession session = ApplicationSession.get();
        filter.add(QueryOptions.Property.VC_OWNER,
                QueryOptions.Relation.EQ,
                session.getUser());
    }

    protected void addTypeFilter(QueryOptions.Filter filter, VirtualCollection.Type type) {
        filter.add(QueryOptions.Property.VC_TYPE,
                QueryOptions.Relation.EQ,
                type);
    }

}
