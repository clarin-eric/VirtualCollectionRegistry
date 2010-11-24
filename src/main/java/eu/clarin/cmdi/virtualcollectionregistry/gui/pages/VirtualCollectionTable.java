package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
public abstract class VirtualCollectionTable extends Panel {
    private class Column extends PropertyColumn<VirtualCollection> {
        public Column(IModel<String> displayModel, String sortProperty,
                String propertyExpression) {
            super(displayModel, sortProperty, propertyExpression);
        }

        public Column(IModel<String> displayModel, String propertyExpression) {
            super(displayModel, propertyExpression);
        }

        @Override
        public String getCssClass() {
            return getPropertyExpression();
        }
    } // class VirtualCollectionTable.Column

    private class ActionsPanel extends Panel {

        public ActionsPanel(String id, IModel<VirtualCollection> model,
                boolean onlyPrivate) {
            super(id, model);
            setRenderBodyOnly(true);

            final AjaxLink<VirtualCollection> publishLink =
                new AjaxLink<VirtualCollection>("publish", model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    doPublish(target, getModelObject());
                }
            };
            add(publishLink);

            final AjaxLink<VirtualCollection> editLink =
                new AjaxLink<VirtualCollection>("edit", model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    doEdit(target, getModelObject());
                }
            };
            add(editLink);

            final AjaxLink<VirtualCollection> deleteLink =
                new AjaxLink<VirtualCollection>("delete", model) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        doDelete(target, getModelObject());
                    }
            };
            add(deleteLink);

            final AjaxLink<VirtualCollection> detailsLink =
                new AjaxLink<VirtualCollection>("details", model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    doDetails(target, getModelObject());
                }
            };
            add(detailsLink);

            VirtualCollection vc = model.getObject();
            if (vc.isDeleted()) {
                detailsLink.setVisible(false).setEnabled(false);
            }
            if (!onlyPrivate || !vc.isPrivate()) {
                editLink.setVisible(false).setEnabled(false);
                publishLink.setVisible(false).setEnabled(false);
                deleteLink.setVisible(false).setEnabled(false);
            }
        }
        
    } // class VirtualCollectionTable.ActionsPanel

    private static final FastDateFormat df =
        FastDateFormat.getInstance("yyyy-MM-dd");
    private final DataTable<VirtualCollection> table;
    
    public VirtualCollectionTable(final String id, final boolean onlyPrivate) {
        super(id);
        setRenderBodyOnly(true);

        // create columns
        List<IColumn<VirtualCollection>> columns =
            new ArrayList<IColumn<VirtualCollection>>();
        columns.add(new Column(new Model<String>("Name"), "name"));
        columns.add(new AbstractColumn<VirtualCollection>(
                new Model<String>("Type")) {
            @Override
            public void populateItem(
                    Item<ICellPopulator<VirtualCollection>> item,
                    String componentId, IModel<VirtualCollection> model) {
                // FIXME: i18n!
                String s;
                switch (model.getObject().getType()) {
                case EXTENSIONAL:
                    s = "extensional";
                    break;
                case INTENSIONAL:
                    s = "intensional";
                    break;
                default:
                    throw new WicketRuntimeException("invalid type");
                }
                item.add(new Label(componentId, s));
            }
        });
        if (onlyPrivate) {
            columns.add(new AbstractColumn<VirtualCollection>(
                    new Model<String>("State")) {
                @Override
                public void populateItem(
                        Item<ICellPopulator<VirtualCollection>> item,
                        String componentId, IModel<VirtualCollection> model) {
                    // FIXME: i18n
                    String s;
                    switch (model.getObject().getState()) {
                    case PUBLIC:
                        s = "public";
                        break;
                    case PUBLIC_PENDING:
                        s = "publishing";
                        break;
                    case PRIVATE:
                        s = "private";
                        break;
                    case DELETED:
                        s = "deleted";
                    default:
                        s = "[" + model.getObject().getState().toString() + "]";
                    }
                    item.add(new Label(componentId, s));
                }
            });
        }
        columns.add(new Column(new Model<String>("Description"), "description"));
        columns.add(new AbstractColumn<VirtualCollection>(
                new Model<String>("Created")) {
            @Override
            public void populateItem(
                    Item<ICellPopulator<VirtualCollection>> item,
                    String componentId, IModel<VirtualCollection> model) {
                item.add(new Label(componentId,
                        df.format(model.getObject().getCreationDate())));
            }

            @Override
            public String getCssClass() {
                return "created";
            }
        });
        columns.add(new HeaderlessColumn<VirtualCollection>() {
            @Override
            public void populateItem(
                    Item<ICellPopulator<VirtualCollection>> item,
                    String compontentId, IModel<VirtualCollection> model) {
                item.add(new ActionsPanel(compontentId, model, onlyPrivate));
            }

            @Override
            public String getCssClass() {
                return "action";
            }
        });

        // data provider
        ISortableDataProvider<VirtualCollection> provider =
            new SortableDataProvider<VirtualCollection>() {
            @Override
            public Iterator<? extends VirtualCollection> iterator(int first,
                    int count) {
                return getCollections(first, count);
            }

            @Override
            public IModel<VirtualCollection> model(VirtualCollection vc) {
                return new Model<VirtualCollection>(vc);
            }

            @Override
            public int size() {
                return getCollectionsCount();
            }
        };

        // table
        table = new AjaxFallbackDefaultDataTable<VirtualCollection>("table",
                columns, provider, 32);
        table.setOutputMarkupId(true);
        add(table);
    }

    public DataTable<VirtualCollection> getTable() {
        return table;
    }

    protected void doPublish(AjaxRequestTarget target, VirtualCollection vc) {
    }

    protected void doEdit(AjaxRequestTarget target, VirtualCollection vc) {
    }

    protected void doDelete(AjaxRequestTarget target, VirtualCollection vc) {
    }

    protected void doDetails(AjaxRequestTarget target, VirtualCollection vc) {
        setResponsePage(new VirtualCollectionDetailsPage(vc, getPage()));
    }

    protected abstract int getCollectionsCount();

    protected abstract Iterator<VirtualCollection> getCollections(int first,
                    int count);

} // class VirtualCollectionTable
