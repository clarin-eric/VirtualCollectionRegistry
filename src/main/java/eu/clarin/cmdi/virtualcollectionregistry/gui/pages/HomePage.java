package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.util.Iterator;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;

public class HomePage extends BasePage {
    private transient VirtualCollectionRegistry vcr =
        VirtualCollectionRegistry.instance();

    @SuppressWarnings("serial")
    public HomePage() {
        super();
        final DataTable<VirtualCollection> collectionsTable =
            new AjaxFallbackDefaultDataTable<VirtualCollection>(
                    "collectionsTable",
                createColumns(),
                new SortableDataProvider<VirtualCollection>() {
                    @Override
                    public Iterator<? extends VirtualCollection> iterator(
                            int first, int count) {
                        try {
                            VirtualCollectionList results =
                                vcr.getVirtualCollections(null, first, count);
                            return results.getItems().iterator();
                        } catch (VirtualCollectionRegistryException e) {
                            throw new WicketRuntimeException(e);
                        }
                    }

                    @Override
                    public IModel<VirtualCollection> model(VirtualCollection vc) {
                        return new Model<VirtualCollection>(vc);
                    }

                    @Override
                    public int size() {
                        try {
                            VirtualCollectionList results =
                                vcr.getVirtualCollections(null, -1, 0);
                            return results.getTotalCount();
                        } catch (VirtualCollectionRegistryException e) {
                            throw new WicketRuntimeException(e);
                        }
                    }
                }, 16);
        collectionsTable.setOutputMarkupId(true);
        add(collectionsTable);

    }

    final FastDateFormat df =
        FastDateFormat.getInstance("yyyy-MM-dd");

    @SuppressWarnings({ "unchecked", "serial" })
    private IColumn<VirtualCollection>[] createColumns() {
        final IColumn<?>[] columns = new IColumn<?>[] {
                new PropertyColumn<VirtualCollection>(
                        new Model<String>("Name"), "name") {
                            @Override
                            public String getCssClass() {
                                return "title";
                            }
                },
                new PropertyColumn<VirtualCollection>(
                        new Model<String>("Type"), "type") {
                    @Override
                    public String getCssClass() {
                        return "type";
                    }
                },
                new PropertyColumn<VirtualCollection>(
                        new Model<String>("Description"), "description") {
                    @Override
                    public String getCssClass() {
                        return "description";
                    }
                },
                new PropertyColumn<VirtualCollection>(
                        new Model<String>("Created"), "creationDate") {
                    @Override
                    public String getCssClass() {
                        return "created";
                    }

                    @Override
                    public void populateItem(
                            Item<ICellPopulator<VirtualCollection>> item,
                            String componentId,
                            IModel<VirtualCollection> model) {
                        item.add(new Label(componentId,
                                df.format(model.getObject().getCreationDate())));
                    }
                }
        };
        return (IColumn<VirtualCollection>[]) columns;
    }

} // class HomePage
