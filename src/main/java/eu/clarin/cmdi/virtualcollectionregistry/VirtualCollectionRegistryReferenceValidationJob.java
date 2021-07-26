package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.model.OrderableComparator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class VirtualCollectionRegistryReferenceValidationJob implements Serializable, Comparable{
    private Resource ref;
    private ReferencesEditor.State state;

    public VirtualCollectionRegistryReferenceValidationJob(Resource ref) {
        this.ref = ref;
        this.state = ReferencesEditor.State.INITIALIZED;
    }

    public ReferencesEditor.State getState() {
        return this.state;
    }

    public synchronized void setState(ReferencesEditor.State newState){
        this.state = newState;
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
}
