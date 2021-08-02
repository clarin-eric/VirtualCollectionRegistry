package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryReferenceValidationJob;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryReferenceValidator;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DecimalConverter;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
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

    private ValidationJobProvider provider;
    //private final transient VirtualCollectionRegistryReferenceValidator validator;

    private final static String LBL_NUM_JOBS = "Number of validation jobs";
    private final static String LBL_NUM_WAITING_JOBS = " Waiting:";
    private final static String LBL_NUM_FINISHED_JOBS = "Finished:";

    public ReferenceValidationPanel(String id, VirtualCollectionRegistryReferenceValidator validator) {
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

        add(new Label("num_finished_jobs_lbl", Model.of(LBL_NUM_FINISHED_JOBS)));
        add(new Label("num_finished_jobs", numJobsFinishedModel));
        add(new Label("num_finished_jobs_pct", numJobsFinishedPctModel) {
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new DecimalConverter();
            }
        });

        List<IColumn<VirtualCollectionRegistryReferenceValidationJob, String>> columns = new ArrayList<>();
        columns.add(new PropertyColumn(new Model("Location"), "ref.ref", "ref.ref"));
        columns.add(new ColumnHttpStatus(new Model("Response"), "httpResponseCode", "httpResponseCode"));
        columns.add(new PropertyColumn(new Model("State"), "state.state", "state.state"));
        columns.add(new ColumnTimestamp(new Model("Timestamp"), "state.timestamp", "state.timestamp"));

        provider = new ValidationJobProvider(validator);
        final DataTable<VirtualCollectionRegistryReferenceValidationJob, String> table =
                new AjaxFallbackDefaultDataTable<>("validation_job_table",
                        columns, provider, 30);
        add(table);

        updateValues(validator);
    }

    public void update(VirtualCollectionRegistryReferenceValidator validator) {
        if(validator != null && validator.getJobs() != null) {
            updateValues(validator);
            provider.update(validator);
        } else {
            logger.debug("Validator is invalid: " + validator == null ? "validator = null" : "validator.getJobs() == null");
        }
    }

    private void updateValues(VirtualCollectionRegistryReferenceValidator validator) {
        if(validator == null) {
            return;
        }
        int totalCount = validator.getJobs().size();
        int waitingCount = 0;
        int finishedCount = 0;
        for(VirtualCollectionRegistryReferenceValidationJob job : validator.getJobs()) {
            ReferencesEditor.State state = job.getState().getState();
            if(state == ReferencesEditor.State.DONE || state == ReferencesEditor.State.FAILED) {
                finishedCount++;
            } else {
                waitingCount++;
            }
        }

        numJobsModel.setObject(totalCount);
        numJobsFinishedModel.setObject(finishedCount);
        numJobsFinishedPctModel.setObject(totalCount == 0 ? 0.0 : (float)finishedCount/(float)totalCount*100.0);
        numJobsWaitingModel.setObject(waitingCount);
        numJobsWaitingPctModel.setObject(totalCount == 0 ? 0.0 : (float)waitingCount/(float)totalCount*100.0);
    }

    class ValidationJobProvider extends SortableDataProvider<VirtualCollectionRegistryReferenceValidationJob, String> {

        class SortableDataProviderComparator implements Comparator<VirtualCollectionRegistryReferenceValidationJob>, Serializable {
            public int compare(final VirtualCollectionRegistryReferenceValidationJob o1, final VirtualCollectionRegistryReferenceValidationJob o2) {
                PropertyModel<Comparable> model1 = new PropertyModel<Comparable>(o1, getSort().getProperty());
                PropertyModel<Comparable> model2 = new PropertyModel<Comparable>(o2, getSort().getProperty());

                int result = model1.getObject().compareTo(model2.getObject());
                if (!getSort().isAscending()) {
                    result = -result;
                }

                return result;
            }
        }

        private transient VirtualCollectionRegistryReferenceValidator validator;
        private SortableDataProviderComparator comparator = new SortableDataProviderComparator();

        public ValidationJobProvider(VirtualCollectionRegistryReferenceValidator validator) {
            this.validator = validator;
            setSort("state.timestamp", SortOrder.DESCENDING);
        }

        public void update(VirtualCollectionRegistryReferenceValidator validator) {
            this.validator = validator;
        }

        @Override
        public Iterator<? extends VirtualCollectionRegistryReferenceValidationJob> iterator(long first, long count) {
            final List<VirtualCollectionRegistryReferenceValidationJob> list = this.validator.getJobs();
            final List<VirtualCollectionRegistryReferenceValidationJob> newList = new ArrayList<>(list);
            Collections.sort(newList, comparator);
            return newList.subList((int)first, (int)first + (int)count).iterator();
        }

        @Override
        public long size() {
            if(this.validator == null) {
                return 0;
            }
            if(this.validator.getJobs() == null) {
                return 0;
            }
            return this.validator.getJobs().size();
        }

        @Override
        public IModel<VirtualCollectionRegistryReferenceValidationJob> model(VirtualCollectionRegistryReferenceValidationJob object) {
            return new AbstractReadOnlyModel<VirtualCollectionRegistryReferenceValidationJob>() {
                @Override
                public VirtualCollectionRegistryReferenceValidationJob getObject() {
                    return object;
                }
            };
        }
    }

    final class ColumnTimestamp extends PropertyColumn<VirtualCollectionRegistryReferenceValidationJob, String> {
        private final IConverter dateConverter = new DateConverter(DateConverter.DF_TIMESTAMP);

        public ColumnTimestamp(IModel<String> displayModel, String sortProperty, String propertyExpression) {
            super(displayModel, sortProperty, propertyExpression);
        }

        @Override
        public void populateItem(Item<ICellPopulator<VirtualCollectionRegistryReferenceValidationJob>> item,
                                 String componentId, IModel<VirtualCollectionRegistryReferenceValidationJob> model) {
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

    final class ColumnHttpStatus extends PropertyColumn<VirtualCollectionRegistryReferenceValidationJob, String> {
        public ColumnHttpStatus(IModel<String> displayModel, String sortProperty, String propertyExpression) {
            super(displayModel, sortProperty, propertyExpression);
        }

        @Override
        public void populateItem(Item<ICellPopulator<VirtualCollectionRegistryReferenceValidationJob>> item,
                                 String componentId, IModel<VirtualCollectionRegistryReferenceValidationJob> model) {
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

    final class ColumnRef extends PropertyColumn<VirtualCollectionRegistryReferenceValidationJob, String> {
        public ColumnRef(IModel<String> displayModel, String sortProperty, String propertyExpression) {
            super(displayModel, sortProperty, propertyExpression);
        }

        @Override
        public String getCssClass() {
            return "ref";
        }
    }
}
