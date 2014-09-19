package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import org.apache.wicket.model.IModel;

/**
 * Provider that dynamically switches between user or public collection spaces
 * on basis of a provided user model.
 * 
 * Permission checking does not happen within this provider!
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class AdminCollectionsProvider extends CollectionsProvider {

    private final IModel<User> userModel;

    public AdminCollectionsProvider(IModel<User> userModel) {
        this.userModel = userModel;
    }

    @Override
    protected void addSpaceFilter(QueryOptions.Filter filter) {
        User user = userModel.getObject();
        if (user == null) {
            // select public collections
            filter.add(QueryOptions.Property.VC_STATE,
                    QueryOptions.Relation.EQ,
                    VirtualCollection.State.PUBLIC);

        } else {
            // select selected user collections
            filter.add(QueryOptions.Property.VC_OWNER,
                    QueryOptions.Relation.EQ,
                    user.getName());

        }
    }

    @Override
    public void detach() {
        userModel.detach();
    }

}
