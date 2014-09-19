package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;

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

}
