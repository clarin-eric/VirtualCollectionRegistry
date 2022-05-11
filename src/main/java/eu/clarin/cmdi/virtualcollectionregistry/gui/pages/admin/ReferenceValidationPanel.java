package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DecimalConverter;
import eu.clarin.cmdi.virtualcollectionregistry.model.ResourceScan;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.*;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.util.convert.IConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.*;

public class ReferenceValidationPanel extends Panel {

    private final static Logger logger = LoggerFactory.getLogger(ReferenceValidationPanel.class);

    private final ValidationJobProvider provider;

    private final ResourceScanSummary summary = new ResourceScanSummary();

    private class ResourceScanSummary implements Serializable {
        private int waiting = 0;
        private int running = 0;
        private int finished = 0;

        public void update(int waiting, int running, int finished) {
            this.waiting = waiting;
            this.running = running;
            this.finished = finished;
        }

        public void update(List<ResourceScan> scans) {
            if(scans == null) {
                return;
            }

            int waiting = 0;
            int running = 0;
            int finished= 0;
            for(ResourceScan scan : scans) {
                ResourceScan.State state = scan.getState();
                switch(state) {
                    case ANALYZING:     running++;  break;
                    case DONE:          finished++; break;
                    case FAILED:        finished++; break;
                    case INITIALIZED:   waiting++;  break;
                }
            }
            update(waiting, running, finished);
        }

        public float getWaiting() {
            return waiting;
        }

        public float getWaitingPct() {
            return (float)(waiting/(float)getTotal()*100.0);
        }

        public int getRunning() {
            return running;
        }

        public float getRunningPct() {
            return (float)(running/(float)getTotal()*100.0);
        }

        public int getFinished() {
            return finished;
        }

        public float getFinishedPct() {
            return (float)(finished/(float)getTotal()*100.0);
        }

        public int getTotal() {
            return waiting+running+finished;
        }
    }

    public ReferenceValidationPanel(String id, List<ResourceScan> scans) {
        super(id);

        add(new Label("summary_heading", new StringResourceModel("summary.heading", this, null)));
        add(new Label("num_jobs_lbl", new StringResourceModel("summary.total", this, null)));
        add(new Label("num_jobs", new PropertyModel<>(summary, "total")));

        add(new Label("num_waiting_jobs_lbl", new StringResourceModel("summary.waiting", this, null)));
        add(new Label("num_waiting_jobs", new PropertyModel<>(summary, "waiting")));
        add(new Label("num_waiting_jobs_pct", new PropertyModel<>(summary, "waitingPct")) {
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new DecimalConverter();
            }
        });

        add(new Label("num_running_jobs_lbl",  new StringResourceModel("summary.running", this, null)));
        add(new Label("num_running_jobs", new PropertyModel(summary,"running")));
        add(new Label("num_running_jobs_pct", new PropertyModel(summary,"runningPct")) {
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new DecimalConverter();
            }
        });

        add(new Label("num_finished_jobs_lbl", new StringResourceModel("summary.finished", this, null)));
        add(new Label("num_finished_jobs", new PropertyModel(summary, "finished")));
        add(new Label("num_finished_jobs_pct", new PropertyModel(summary, "finishedPct")) {
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new DecimalConverter();
            }
        });

        List<IColumn<ResourceScan, String>> columns = new ArrayList<>();
        columns.add(new PropertyColumn(new StringResourceModel("table.column.ref"), "ref", "ref"));
        columns.add(new ColumnTimestamp(new StringResourceModel("table.column.lastScanStart"), "lastScanStart", "lastScanStart"));
        columns.add(new PropertyColumn(new StringResourceModel("table.column.state"), "state", "state"));
        columns.add(new ColumnHttpStatus(new StringResourceModel("table.column.httpResponseCode"), "httpResponseCode", "httpResponseCode"));
        columns.add(new PropertyColumn(new StringResourceModel("table.column.httpResponseMessage"), "httpResponseMessage", "httpResponseMessage"));
        columns.add(new PropertyColumn(new StringResourceModel("table.column.exception"), "exception", "exception"));

        add(new Label("table_heading", new StringResourceModel("table.heading", this, null)));
        provider = new ValidationJobProvider(scans);
        final DataTable<ResourceScan, String> table =
                new AjaxFallbackDefaultDataTable<>("validation_job_table",
                        columns, provider, 30);
        add(table);

        summary.update(scans);
    }

    public ResourceScanSummary getSummary() {
        return summary;
    }

    public void update(List<ResourceScan> scans) {
        if(scans != null) {
            summary.update(scans);
            provider.update(scans);
        } else {
            logger.debug("Validator is invalid: validator = null");
        }
    }

    class ValidationJobProvider extends SortableDataProvider<ResourceScan, String> {

        class SortableDataProviderComparator implements Comparator<ResourceScan>, Serializable {
            public int compare(final ResourceScan o1, final ResourceScan o2) {
                if(getSort() == null) {
                    return 0;
                }

                PropertyModel<Comparable> model1 = new PropertyModel<Comparable>(o1, getSort().getProperty());
                PropertyModel<Comparable> model2 = new PropertyModel<Comparable>(o2, getSort().getProperty());

                int result = model1.getObject().compareTo(model2.getObject());
                if (!getSort().isAscending()) {
                    result = -result;
                }

                return result;
            }
        }

        private transient List<ResourceScan> scans;
        private SortableDataProviderComparator comparator = new SortableDataProviderComparator();

        public ValidationJobProvider(List<ResourceScan> scans) {
            this.scans = scans;
            Collections.sort(this.scans, comparator);
            setSort("lastScanStart", SortOrder.DESCENDING);
        }

        public void update(List<ResourceScan> scans) {
            this.scans = scans;
            Collections.sort(this.scans, comparator);
        }

        @Override
        public Iterator<? extends ResourceScan> iterator(long first, long count) {
            return scans.subList((int)first, (int)first + (int)count).iterator();
        }

        @Override
        public long size() {
            if(this.scans == null) {
                return 0;
            }
            return this.scans.size();
        }

        @Override
        public IModel<ResourceScan> model(ResourceScan object) {
            return new AbstractReadOnlyModel<ResourceScan>() {
                @Override
                public ResourceScan getObject() {
                    return object;
                }
            };
        }
    }

    final class ColumnTimestamp extends PropertyColumn<ResourceScan, String> {
        private final IConverter dateConverter = new DateConverter(DateConverter.DF_TIMESTAMP);

        public ColumnTimestamp(IModel<String> displayModel, String sortProperty, String propertyExpression) {
            super(displayModel, sortProperty, propertyExpression);
        }

        @Override
        public void populateItem(Item<ICellPopulator<ResourceScan>> item,
                                 String componentId, IModel<ResourceScan> model) {
            item.add(new Label(componentId, this.getDataModel(model)) {
                @SuppressWarnings("unchecked")
                @Override
                public <C> IConverter<C> getConverter(Class<C> type) {
                    if (Date.class.isAssignableFrom(type)) {
                        return dateConverter;
                    }
                    return super.getConverter(type);
                }

            });
        }

        @Override
        public String getCssClass() {
            return "timestamp";
        }
    }

    final class ColumnHttpStatus extends PropertyColumn<ResourceScan, String> {
        public ColumnHttpStatus(IModel<String> displayModel, String sortProperty, String propertyExpression) {
            super(displayModel, sortProperty, propertyExpression);
        }

        @Override
        public void populateItem(Item<ICellPopulator<ResourceScan>> item,
                                 String componentId, IModel<ResourceScan> model) {
            IModel<String> lblModel = Model.of("");
            int status = (Integer)this.getDataModel(model).getObject();
            if(status > 0) {
                lblModel = Model.of("HTTP " + status);
            }
            item.add(new Label(componentId, lblModel));
        }

        @Override
        public String getCssClass() {
            return "timestamp";
        }
    }

    final class ColumnRef extends PropertyColumn<ResourceScan, String> {
        public ColumnRef(IModel<String> displayModel, String sortProperty, String propertyExpression) {
            super(displayModel, sortProperty, propertyExpression);
        }

        @Override
        public String getCssClass() {
            return "ref";
        }
    }
}
