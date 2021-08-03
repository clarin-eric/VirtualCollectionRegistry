package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.model.OrderableComparator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VirtualCollectionRegistryReferenceValidationJob implements Serializable, Comparable {
    private Resource ref;
    private final String id;
    private final List<JobState> states = new ArrayList<>(); //keep track of this jobs state history
    private int httpResponseCode;
    private String httpResponseReason;

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public String getHttpResponseReason() {
        return httpResponseReason;
    }

    public void setHttpResponseReason(String httpResponseReason) {
        this.httpResponseReason = httpResponseReason;
    }

    public class JobState implements Serializable {
        private final Date timestamp;
        private final ReferencesEditor.State state;
        private String data;

        public JobState(ReferencesEditor.State state) {
            this(state, null);
        }

        public JobState(ReferencesEditor.State state, String data) {
            this.state = state;
            this.setData(data);
            this.timestamp = new Date();
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public ReferencesEditor.State getState() {
            return state;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    public VirtualCollectionRegistryReferenceValidationJob(Resource ref, String id) {
        this.ref = ref;
        this.id = id;
        states.add(new JobState(ReferencesEditor.State.INITIALIZED));
    }

    public JobState getState() {
        return states.get(states.size()-1);
    }

    public synchronized void setState(ReferencesEditor.State newState){
        states.add(new JobState(newState));
    }

    public synchronized void setState(ReferencesEditor.State newState, String data){
        states.add(new JobState(newState, data));
    }

    public Resource getReference() {
        return this.ref;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if( o == null) return 0;
        if(o instanceof VirtualCollectionRegistryReferenceValidationJob) {
            return OrderableComparator.compare(
                    getReference(),
                    ((VirtualCollectionRegistryReferenceValidationJob) o).getReference());
        }
        return 0;
    }

    public String getId() {
        return id;
    }

    public List<JobState> getStates() {
        return states;
    }
}
