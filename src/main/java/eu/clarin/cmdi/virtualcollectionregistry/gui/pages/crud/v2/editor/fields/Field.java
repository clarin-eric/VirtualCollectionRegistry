package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

/**
 *
 * @author wilelb
 */
public interface Field {
    void setRequired(boolean required);
    boolean validate();
    void updateVisability();
}
