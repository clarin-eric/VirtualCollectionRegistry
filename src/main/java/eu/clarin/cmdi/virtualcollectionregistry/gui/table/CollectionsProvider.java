package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.*;
import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions.Property;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DetachableVirtualCollectionModel;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class CollectionsProvider extends
        SortableDataProvider<VirtualCollection, String> implements
        IFilterStateLocator<FilterState> {

    private final static Logger logger = LoggerFactory.getLogger(CollectionsProvider.class);

    private FilterState filterstate = new FilterState();

    public CollectionsProvider() {
        setSort("created", SortOrder.DESCENDING);
    }

    @Override
    public FilterState getFilterState() {
        return filterstate;
    }

    @Override
    public void setFilterState(FilterState state) { this.filterstate = state; }

    @Override
    public IModel<VirtualCollection> model(VirtualCollection vc) {
        return new DetachableVirtualCollectionModel(vc);
    }

    public List<String> getOrigins() {
        final VirtualCollectionRegistry vcr = Application.get().getRegistry();
        List<String> origins = new ArrayList<>();
        try {
            origins = vcr.getOrigins();
        } catch(VirtualCollectionRegistryException ex) {
            logger.error("", ex);
        }
        return origins;
    }

    @Override
    public long size() {
        try {
            final VirtualCollectionRegistry vcr = Application.get().getRegistry();
            return vcr.getVirtualCollectionCount(getQueryFactory());
        } catch (VirtualCollectionRegistryException e) {
            throw new WicketRuntimeException(e);
        }
    }

    public List<VirtualCollection> getList() {
        try {
            final VirtualCollectionRegistry vcr
                    = Application.get().getRegistry();
            final List<VirtualCollection> results
                    = vcr.getVirtualCollections(0, (int)size(), getQueryFactory());
            return results;
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
                    = vcr.getVirtualCollections((int)first, (int)count, getQueryFactory());
            return results.iterator();
        } catch (VirtualCollectionRegistryException e) {
            throw new WicketRuntimeException(e);
        }
    }

    private QueryFactory getQueryFactory() {
        QueryFactory factory = new QueryFactory();

        //Add filter to only query collections without a child, i.e. "latest" versions
        /*
        id      | root   | parent | child | state   | ...
        ----------------------------
        0       | 0      | NULL   | NULL  | public  | ...

        1       | 1      | NULL   | 2     | public  | ...
        2       | 1      | 1      | 3     | public  | ...
        3       | 1      | 2      | NULL  | public  | ...

        4       | 1      | NULL   | 5     | public  | ...
        5       | 1      | 4      | 6     | public  | ...
        6       | 1      | 6      | NULL  | private | ...
         */
        factory.andIsNull(QueryOptions.Property.VC_CHILD);

        // add the filter that selects the public or private space (overridden)
        addSpaceFilter(factory);

        // apply the filter state
        if (filterstate.hasName()) {
            factory.and(QueryOptions.Property.VC_NAME, QueryOptions.Relation.EQ, filterstate.getNameWithWildcard());
        }
        if (filterstate.hasType()) {
            factory.and(QueryOptions.Property.VC_TYPE, QueryOptions.Relation.EQ, filterstate.getType());
        }
        if (filterstate.hasState()) {
            factory.and(QueryOptions.Property.VC_STATE, QueryOptions.Relation.IN, filterstate.getState());
        }
        if (filterstate.hasDescription()) {
            factory.and(QueryOptions.Property.VC_DESCRIPTION, QueryOptions.Relation.EQ, filterstate.getDescriptionWithWildcard());
        }
        if (filterstate.hasCreated()) {
            factory.and(QueryOptions.Property.VC_CREATION_DATE, filterstate.getCreatedRelation(), filterstate.getCreated());
        }
        if (filterstate.hasOrigin()) {
            factory.and(QueryOptions.Property.VC_ORIGIN, QueryOptions.Relation.EQ, filterstate.getOrigin());
        }

        final SortParam<String> s = getSort();
        if (s != null) {
            final String p = s.getProperty();
            if ("name".equals(p)) {
                factory.addSortProperty(Property.VC_NAME, s.isAscending());
            } else if ("type".equals(p)) {
                factory.addSortProperty(Property.VC_TYPE, s.isAscending());
            } else if ("state".equals(p)) {
                factory.addSortProperty(Property.VC_STATE, s.isAscending());
            } else if ("description".equals(p)) {
                factory.addSortProperty(Property.VC_DESCRIPTION, s.isAscending());
            } else if ("created".equals(p)) {
                factory.addSortProperty(Property.VC_CREATION_DATE, s.isAscending());
            }
        }

        return factory;
    }

    /**
     * Adds a filter that limits the results to a specific collections space
     * ({@lit i.e.} the public space or a user's private work space
     * @param factory
     */
    protected abstract void addSpaceFilter(QueryFactory factory);

} // class VirtualCollectionProvider
