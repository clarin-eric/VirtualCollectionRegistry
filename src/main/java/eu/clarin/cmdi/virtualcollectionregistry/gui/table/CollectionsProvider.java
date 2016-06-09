package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions.Property;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DetachableVirtualCollectionModel;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class CollectionsProvider extends
        SortableDataProvider<VirtualCollection, String> implements
        IFilterStateLocator<FilterState> {

    private FilterState filterstate = new FilterState();

    public CollectionsProvider() {
        setSort("created", SortOrder.DESCENDING);
    }

    @Override
    public FilterState getFilterState() {
        return filterstate;
    }

    @Override
    public void setFilterState(FilterState state) {
        this.filterstate = state;
    }

    @Override
    public IModel<VirtualCollection> model(VirtualCollection vc) {
        return new DetachableVirtualCollectionModel(vc);
    }

    @Override
    public long size() {
        try {
            final VirtualCollectionRegistry vcr
                    = Application.get().getRegistry();
            return vcr.getVirtualCollectionCount(getFilter());
        } catch (VirtualCollectionRegistryException e) {
            throw new WicketRuntimeException(e);
        }
    }

    @Override
    public Iterator<? extends VirtualCollection> iterator(long first,
            long count) {
        try {
            final VirtualCollectionRegistry vcr
                    = Application.get().getRegistry();
            final List<VirtualCollection> results
                    = vcr.getVirtualCollections((int)first, (int)count, getFilter());
            return results.iterator();
        } catch (VirtualCollectionRegistryException e) {
            throw new WicketRuntimeException(e);
        }
    }

    private QueryOptions getFilter() {
        QueryOptions options = new QueryOptions();

        QueryOptions.Filter filter = options.and();
        // add the filter that selects the public or private space
        addSpaceFilter(filter);
        // apply the filter state
        if (filterstate.hasName()) {
            filter.add(QueryOptions.Property.VC_NAME,
                    QueryOptions.Relation.EQ,
                    filterstate.getNameWithWildcard());
        }
        if (filterstate.hasType()) {
            filter.add(QueryOptions.Property.VC_TYPE,
                    QueryOptions.Relation.EQ,
                    filterstate.getType());
        }
        if (filterstate.hasState()) {
            filter.add(QueryOptions.Property.VC_STATE,
                    QueryOptions.Relation.IN,
                    filterstate.getState());
        }
        if (filterstate.hasDescription()) {
            filter.add(QueryOptions.Property.VC_DESCRIPTION,
                    QueryOptions.Relation.EQ,
                    filterstate.getDescriptionWithWildcard());
        }
        if (filterstate.hasCreated()) {
            filter.add(QueryOptions.Property.VC_CREATION_DATE,
                    filterstate.getCreatedRelation(),
                    filterstate.getCreated());
        }
        options.setFilter(filter);

        final SortParam<String> s = getSort();
        if (s != null) {
            final String p = s.getProperty();
            Property property = null;
            if ("name".equals(p)) {
                property = Property.VC_NAME;
            } else if ("type".equals(p)) {
                property = Property.VC_TYPE;
            } else if ("state".equals(p)) {
                property = Property.VC_STATE;
            } else if ("description".equals(p)) {
                property = Property.VC_DESCRIPTION;
            } else if ("created".equals(p)) {
                property = Property.VC_CREATION_DATE;
            }
            if (property != null) {
                options.addSortProperty(property, s.isAscending());
            }
        }
        return options;
    }

    /**
     * Adds a filter that limits the results to a specific collections space
     * ({@lit i.e.} the public space or a user's private work space
     * @param filter 
     */
    protected abstract void addSpaceFilter(QueryOptions.Filter filter);

} // class VirtualCollectionProvider
