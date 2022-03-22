package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface MoveListEventHandler {
    public void handleMoveUp(Long displayOrder, AjaxRequestTarget target);
    public void handleMoveDown(Long displayOrder, AjaxRequestTarget target);
    public void handleMoveTop(Long displayOrder, AjaxRequestTarget target);
    public void handleMoveEnd(Long displayOrder, AjaxRequestTarget target);
}
