package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

/**
 *
 * @author wilelb
 */
public interface Field {
    String getName();
    boolean isRequired();
    void setRequired(boolean required);
    boolean validate();
    void updateVisability();
    void showHelp(boolean showHelp);
}
