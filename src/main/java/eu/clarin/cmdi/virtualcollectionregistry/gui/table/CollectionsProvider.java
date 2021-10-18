package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.*;
import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions.Property;
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
    public void setFilterState(FilterState state) { this.filterstate = state; }

    @Override
    public IModel<VirtualCollection> model(VirtualCollection vc) {
        return new DetachableVirtualCollectionModel(vc);
    }

    public List<String> getOrigins() {
        final VirtualCollectionRegistry vcr = Application.get().getRegistry();
        return vcr.getOrigins();
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

        // add the filter that selects the public or private space
        addSpaceFilter(factory);

        // apply the filter state
        if (filterstate.hasName()) {
            /*filter.add(QueryOptions.Property.VC_NAME,
                    QueryOptions.Relation.EQ,
                    filterstate.getNameWithWildcard());*/
            factory.and(QueryOptions.Property.VC_NAME, QueryOptions.Relation.EQ, filterstate.getNameWithWildcard());
        }
        if (filterstate.hasType()) {
            /*filter.add(QueryOptions.Property.VC_TYPE,
                    QueryOptions.Relation.EQ,
                    filterstate.getType());*/
            factory.and(QueryOptions.Property.VC_TYPE, QueryOptions.Relation.EQ, filterstate.getType());
        }
        if (filterstate.hasState()) {
            /*filter.add(QueryOptions.Property.VC_STATE,
                    QueryOptions.Relation.IN,
                    filterstate.getState());*/
            factory.and(QueryOptions.Property.VC_STATE, QueryOptions.Relation.IN, filterstate.getState());
        }
        if (filterstate.hasDescription()) {
            /*filter.add(QueryOptions.Property.VC_DESCRIPTION,
                    QueryOptions.Relation.EQ,
                    filterstate.getDescriptionWithWildcard());*/
            factory.and(QueryOptions.Property.VC_DESCRIPTION, QueryOptions.Relation.EQ, filterstate.getDescriptionWithWildcard());
        }
        if (filterstate.hasCreated()) {
            /*filter.add(QueryOptions.Property.VC_CREATION_DATE,
                    filterstate.getCreatedRelation(),
                    filterstate.getCreated());*/
            factory.and(QueryOptions.Property.VC_CREATION_DATE, filterstate.getCreatedRelation(), filterstate.getCreated());
        }
        if (filterstate.hasOrigin()) {
            /*filter.add(Property.VC_ORIGIN,
                    QueryOptions.Relation.EQ,
                    filterstate.getOrigin());*/
            factory.and(QueryOptions.Property.VC_ORIGIN, QueryOptions.Relation.EQ, filterstate.getOrigin());
        }

        final SortParam<String> s = getSort();
        if (s != null) {
            final String p = s.getProperty();
            //Property property = null;
            if ("name".equals(p)) {
                //property = Property.VC_NAME;
                factory.addSortProperty(Property.VC_NAME, s.isAscending());
            } else if ("type".equals(p)) {
                //property = Property.VC_TYPE;
                factory.addSortProperty(Property.VC_TYPE, s.isAscending());
            } else if ("state".equals(p)) {
                //property = Property.VC_STATE;
                factory.addSortProperty(Property.VC_STATE, s.isAscending());
            } else if ("description".equals(p)) {
                //property = Property.VC_DESCRIPTION;
                factory.addSortProperty(Property.VC_DESCRIPTION, s.isAscending());
            } else if ("created".equals(p)) {
                //property = Property.VC_CREATION_DATE;
                factory.addSortProperty(Property.VC_CREATION_DATE, s.isAscending());
            }
            //if (property != null) {
            //    options.addSortProperty(property, s.isAscending());
            //}
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
