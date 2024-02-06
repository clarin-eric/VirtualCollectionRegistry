package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields;

import java.io.Serializable;

/**
 *
 * @author wilelb
 */
public interface InputValidator extends Serializable {
    public boolean validate(String input);
    public String getErrorMessage();
}
