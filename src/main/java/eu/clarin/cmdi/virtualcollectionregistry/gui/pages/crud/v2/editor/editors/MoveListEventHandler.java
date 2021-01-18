package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface MoveListEventHandler {
    public void handleMoveUp(Long id, AjaxRequestTarget target);
    public void handleMoveDown(Long id, AjaxRequestTarget target);
    public void handleMoveTop(Long id, AjaxRequestTarget target);
    public void handleMoveEnd(Long id, AjaxRequestTarget target);
}
