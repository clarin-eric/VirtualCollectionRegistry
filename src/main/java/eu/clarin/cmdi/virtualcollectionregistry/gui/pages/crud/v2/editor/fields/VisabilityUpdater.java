package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import org.apache.wicket.Component;

import java.io.Serializable;

public interface VisabilityUpdater extends Serializable {
    void updateVisability(Component componentToUpdate);
}
