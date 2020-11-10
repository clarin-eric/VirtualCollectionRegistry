package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

/**
 *
 * @author wilelb
 */
public interface InputValidator {
    public boolean validate(String input);
    public String getErrorMessage();
}
