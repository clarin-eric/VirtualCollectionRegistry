package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

/**
 *
 * @author wilelb
 */
public interface Field {
    public void setRequired(boolean required);
    public boolean validate();
}
