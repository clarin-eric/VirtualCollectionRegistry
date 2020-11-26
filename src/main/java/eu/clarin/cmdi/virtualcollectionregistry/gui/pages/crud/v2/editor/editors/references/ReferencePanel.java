package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.EventHandler;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 *
 * @author wilelb
 */
public class ReferencePanel extends Panel {
    private static Logger logger = LoggerFactory.getLogger(ReferencePanel.class);
    
    private transient List<EventHandler> eventHandlers = new ArrayList<>();
    
    /**
     * 
     * @param id    The wicket component id
     * @param ref 
     */
    public ReferencePanel(String id, final ReferencesEditor.ReferenceJob ref, Model<Boolean> advancedEditorMode, boolean moveActionsEnabled) {
        super(id);

        Model titleModel = Model.of("<required>");
        if(ref.getReference().getLabel() != null) {
            titleModel.setObject(ref.getReference().getLabel());
        }
        Model descriptionModel = Model.of("<required>");
        if(ref.getReference().getDescription() != null) {
            descriptionModel.setObject(ref.getReference().getDescription());
        }
        
        WebMarkupContainer editorWrapper = new WebMarkupContainer("wrapper");

        String check = ref.getReference().getCheck();

        boolean analysing = false;
        if(ref.getState() == ReferencesEditor.State.INITIALIZED || ref.getState() == ReferencesEditor.State.ANALYZING) {
            analysing = true;
        }



        WebMarkupContainer stateIcon = new WebMarkupContainer("state");
        switch(ref.getState()) {
            case DONE:
                stateIcon.add(new AttributeAppender("class", "fa fa-check-circle-o icon-success"));
                break;
            case FAILED:
                stateIcon.add(new AttributeAppender("class", "fa fa-times-circle-o icon-failed"));
                break;
            default:
                stateIcon.add(new AttributeAppender("class", "fa fa-dot-circle-o icon-waiting"));
                break;
        }
        editorWrapper.add(stateIcon);

        String urlValue = ref.getReference().getRef();
        if(!titleModel.getObject().toString().isEmpty()) {
            urlValue = "("+ref.getReference().getRef()+")";
        }
        editorWrapper.add(new Label("value", urlValue));

        Label lblWaiting = new Label("lbl_waiting", "Waiting on analysis");
        lblWaiting.setVisible(analysing);
        editorWrapper.add(lblWaiting);

        Label lblTypeLabel = new Label("lbl_type", "Type:");
        lblTypeLabel.setVisible(!analysing && advancedEditorMode.getObject());
        editorWrapper.add(lblTypeLabel);
        Label lblType = new Label("type", ref.getReference().getType());
        lblType.setVisible(!analysing && advancedEditorMode.getObject());
        editorWrapper.add(lblType);

        Label lblTitle = new Label("title", titleModel);
        lblTitle.setVisible(!analysing);
        editorWrapper.add(lblTitle);

        Label labelDescription = new Label("description", descriptionModel);
        labelDescription.setVisible(!analysing);
        editorWrapper.add(labelDescription);

        AjaxFallbackLink orderTopButton = new AjaxFallbackLink("btn_order_top") {
            @Override
            public void onClick(AjaxRequestTarget target) {

            }
        };
        orderTopButton.setVisible(!analysing);
        orderTopButton.setEnabled(moveActionsEnabled);
        editorWrapper.add(orderTopButton);
        AjaxFallbackLink orderUpButton = new AjaxFallbackLink("btn_order_up") {
            @Override
            public void onClick(AjaxRequestTarget target) {

            }
        };
        orderUpButton.setVisible(!analysing);
        orderUpButton.setEnabled(moveActionsEnabled);
        editorWrapper.add(orderUpButton);
        AjaxFallbackLink orderDownButton = new AjaxFallbackLink("btn_order_down") {
            @Override
            public void onClick(AjaxRequestTarget target) {

            }
        };
        orderDownButton.setVisible(!analysing);
        orderDownButton.setEnabled(moveActionsEnabled);
        editorWrapper.add(orderDownButton);
        AjaxFallbackLink orderBottomButton = new AjaxFallbackLink("btn_order_bottom") {
            @Override
            public void onClick(AjaxRequestTarget target) {

            }
        };
        orderBottomButton.setVisible(!analysing);
        orderBottomButton.setEnabled(moveActionsEnabled);
        editorWrapper.add(orderBottomButton);

        AjaxFallbackLink btnEdit = new AjaxFallbackLink("btn_edit") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(EventHandler handler : eventHandlers) {
                    handler.handleEditEvent(ref.getReference(), target);
                }
            }
        };
        btnEdit.setEnabled(ref.getState() == ReferencesEditor.State.DONE);
        btnEdit.setVisible(!analysing);
        editorWrapper.add(btnEdit);
        
        AjaxFallbackLink btnRemove = new AjaxFallbackLink("btn_remove") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(EventHandler handler : eventHandlers) {
                    handler.handleRemoveEvent(ref.getReference(), target);
                }
            }
        };
        btnRemove.setEnabled(ref.getState() == ReferencesEditor.State.DONE);
        btnRemove.setVisible(!analysing);
        editorWrapper.add(btnRemove);

        WebMarkupContainer spinner = new WebMarkupContainer("spinner");
        spinner.setVisible(analysing);
        editorWrapper.add(spinner);

        add(editorWrapper);
    }
    
    public void addEventHandler(EventHandler handler) {
        this.eventHandlers.add(handler);
    }
}
