package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin.AdminPage;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryOptions;
import java.util.LinkedList;
import java.util.List;

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
    protected void addSpaceFilter(QueryFactory factory) {
        User user = userModel.getObject();
        if (user == null || user.getName().equalsIgnoreCase(AdminPage.PUBLIC_USER.getName())) {
            List<VirtualCollection.State> states = new LinkedList<>();
            states.add(VirtualCollection.State.PUBLIC);
            states.add(VirtualCollection.State.PUBLIC_FROZEN);
            states.add(VirtualCollection.State.ERROR);
            // select public collections
            factory.and(QueryOptions.Property.VC_STATE, QueryOptions.Relation.IN, states);
        } else {
            // select selected user collections
            factory.and(QueryOptions.Property.VC_OWNER, QueryOptions.Relation.EQ, user.getName());
        }
    }

    @Override
    public void detach() {
        userModel.detach();
    }

}
