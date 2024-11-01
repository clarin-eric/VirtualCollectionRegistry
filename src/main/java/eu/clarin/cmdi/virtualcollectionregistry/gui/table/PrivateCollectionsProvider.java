package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.security.Principal;

/**
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class PrivateCollectionsProvider extends CollectionsProvider {

    private final String authenticatedUsername;
    
    public PrivateCollectionsProvider(final Principal authenticatedPrincipal) {
        this(authenticatedPrincipal.getName());
    }
    
    public PrivateCollectionsProvider(final String authenticatedUsername) {
        this.authenticatedUsername = authenticatedUsername;
    }
    
    @Override
    protected void addSpaceFilter(QueryOptions.Filter filter) {
        filter.add(QueryOptions.Property.VC_OWNER,
                QueryOptions.Relation.EQ,
                authenticatedUsername);
    }

    protected void addTypeFilter(QueryOptions.Filter filter, VirtualCollection.Type type) {
        filter.add(QueryOptions.Property.VC_TYPE,
                QueryOptions.Relation.EQ,
                type);
    }

}
