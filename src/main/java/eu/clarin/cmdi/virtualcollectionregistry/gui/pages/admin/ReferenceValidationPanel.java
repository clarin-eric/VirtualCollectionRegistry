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
    private final IModel numJobsModel = Model.of(0);
    private final IModel numJobsWaitingModel = Model.of(0);
    private final IModel numJobsWaitingPctModel = Model.of(0.0);
    private final IModel numJobsFinishedModel = Model.of(0);
    private final IModel numJobsFinishedPctModel = Model.of(0.0);
    private final IModel numJobsRunningModel = Model.of(0.0);
    private final IModel numJobsRunningPctModel = Model.of(0.0);

    private ValidationJobProvider provider;

    private final static String LBL_NUM_JOBS = "Number of validation jobs";
    private final static String LBL_NUM_WAITING_JOBS = " Waiting:";
    private final static String LBL_NUM_RUNNING_JOBS = " Running:";
    private final static String LBL_NUM_FINISHED_JOBS = "Finished:";

    public ReferenceValidationPanel(String id, List<ResourceScan> scans) {
        super(id);

        add(new Label("num_jobs", numJobsModel));
        add(new Label("num_jobs_lbl", Model.of(LBL_NUM_JOBS)));

        add(new Label("num_waiting_jobs_lbl", Model.of(LBL_NUM_WAITING_JOBS)));
        add(new Label("num_waiting_jobs", numJobsWaitingModel));
        add(new Label("num_waiting_jobs_pct", numJobsWaitingPctModel)  {
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new DecimalConverter();
            }
        });

        add(new Label("num_running_jobs_lbl", Model.of(LBL_NUM_RUNNING_JOBS)));
        add(new Label("num_running_jobs", numJobsRunningModel));
        add(new Label("num_running_jobs_pct", numJobsRunningPctModel) {
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new DecimalConverter();
            }
        });

        add(new Label("num_finished_jobs_lbl", Model.of(LBL_NUM_FINISHED_JOBS)));
        add(new Label("num_finished_jobs", numJobsFinishedModel));
        add(new Label("num_finished_jobs_pct", numJobsFinishedPctModel) {
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new DecimalConverter();
            }
        });

        List<IColumn<ResourceScan, String>> columns = new ArrayList<>();
        columns.add(new PropertyColumn(new Model("Location"), "ref", "ref"));
        columns.add(new ColumnTimestamp(new Model("Last Scan"), "lastScanStart", "lastScanStart"));
        columns.add(new PropertyColumn(new Model("State"), "state", "state"));
        columns.add(new ColumnHttpStatus(new Model("Response"), "httpResponseCode", "httpResponseCode"));
        columns.add(new PropertyColumn(new Model("Response"), "httpResponseMessage", "httpResponseMessage"));
        columns.add(new PropertyColumn(new Model("Exception"), "exception", "exception"));

        provider = new ValidationJobProvider(scans);
        final DataTable<ResourceScan, String> table =
                new AjaxFallbackDefaultDataTable<>("validation_job_table",
                        columns, provider, 30);
        add(table);

        updateValues(scans);
    }

    public void update(List<ResourceScan> scans) {
        if(scans != null) {
            updateValues(scans);
            provider.update(scans);
        } else {
            logger.debug("Validator is invalid: validator = null");
        }

    }

    private void updateValues(List<ResourceScan> scans) {
        if(scans == null) {
            return;
        }
        int totalCount = scans.size();
        int waitingCount = 0;
        int runningCount = 0;
        int finishedCount = 0;

        for(ResourceScan scan : scans) {
            ResourceScan.State state = scan.getState();
            switch(state) {
                case ANALYZING:     runningCount++;     break;
                case DONE:          finishedCount++;    break;
                case FAILED:        finishedCount++;    break;
                case INITIALIZED:   waitingCount++;     break;
            }
        }
        numJobsModel.setObject(totalCount);
        numJobsWaitingModel.setObject(waitingCount);
        numJobsWaitingPctModel.setObject(totalCount == 0 ? 0.0 : (float)waitingCount/(float)totalCount*100.0);
        numJobsRunningModel.setObject(runningCount);
        numJobsRunningPctModel.setObject(totalCount == 0 ? 0.0 : (float)runningCount/(float)totalCount*100.0);
        numJobsFinishedModel.setObject(finishedCount);
        numJobsFinishedPctModel.setObject(totalCount == 0 ? 0.0 : (float)finishedCount/(float)totalCount*100.0);

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
