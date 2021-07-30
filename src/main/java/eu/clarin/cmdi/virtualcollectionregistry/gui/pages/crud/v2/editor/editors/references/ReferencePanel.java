package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import java.util.ArrayList;
import java.util.List;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryReferenceValidationJob;
import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.EventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.MoveListEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
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
    private transient List<MoveListEventHandler> moveListEventHandlers = new ArrayList<>();
    
    /**
     * 
     * @param id    The wicket component id
     * @param ref 
     */
    public ReferencePanel(String id, final Resource ref, final ReferencesEditor.State state, Model<Boolean> advancedEditorMode, long maxDisplayOrder) {
        super(id);
        long displayOrder = ref.getDisplayOrder();

        Model titleModel = Model.of("<required>");
        if(ref.getLabel() != null) {
            titleModel.setObject(ref.getLabel());
        }
        Model descriptionModel = Model.of("<required>");
        if(ref.getDescription() != null) {
            descriptionModel.setObject(ref.getDescription());
        }
        
        WebMarkupContainer editorWrapper = new WebMarkupContainer("wrapper");

        boolean analysing = false;
        if(state == ReferencesEditor.State.INITIALIZED || state == ReferencesEditor.State.ANALYZING) {
            analysing = true;
        }

        WebMarkupContainer stateIcon = new WebMarkupContainer("state");
        switch(state) {
            case DONE:
                if(HandleLinkModel.isSupportedPersistentIdentifier(ref.getRef())) {
                    stateIcon.add(new AttributeAppender("class", "fa fa-check-circle-o icon icon-success"));
                } else {
                    stateIcon.add(new AttributeAppender("class", "fa fa-check-circle-o icon icon-passed"));
                    //TODO: make clickable and show popup with details
                }
                break;
            case FAILED:
                stateIcon.add(new AttributeAppender("class", "fa fa-times-circle-o icon icon-failed"));
                break;
            default:
                stateIcon.add(new AttributeAppender("class", "fa fa-dot-circle-o icon icon-waiting"));
                break;
        }
        editorWrapper.add(stateIcon);

        String urlValue = ref.getRef();
        if(!titleModel.getObject().toString().isEmpty()) {
            urlValue = "("+ref.getRef()+")";
        }
        editorWrapper.add(new Label("value", urlValue));

        Label lblWaiting = new Label("lbl_waiting", "Waiting on analysis");
        lblWaiting.setVisible(analysing);
        editorWrapper.add(lblWaiting);

        Label lblTypeLabel = new Label("lbl_type", "Type:");
        lblTypeLabel.setVisible(!analysing && advancedEditorMode.getObject());
        editorWrapper.add(lblTypeLabel);
        Label lblType = new Label("type", ref.getType());
        lblType.setVisible(!analysing && advancedEditorMode.getObject());
        editorWrapper.add(lblType);

        Label lblTitle = new Label("title", titleModel);
        lblTitle.setVisible(!analysing);
        editorWrapper.add(lblTitle);

        Label lblMerged = new Label("merged", "This resource was merged from the submitted collection.");
        lblMerged.setVisible(ref.isMerged());
        editorWrapper.add(lblMerged);

        String htmlValue = "";
        if(descriptionModel.getObject() != null) {
            MutableDataSet options = new MutableDataSet();
            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();
            Node document = parser.parse(descriptionModel.getObject().toString());
            htmlValue = renderer.render(document);
        }

        Label labelDescription = new Label("description", Model.of(htmlValue));
        labelDescription.setEscapeModelStrings(false);
        labelDescription.setVisible(!analysing);
        editorWrapper.add(labelDescription);

        AjaxFallbackLink orderTopButton = new AjaxFallbackLink("btn_order_top") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(MoveListEventHandler handler : moveListEventHandlers) {
                    handler.handleMoveTop(ref.getDisplayOrder(), target);
                }
            }
        };
        orderTopButton.setVisible(!analysing);
        if(displayOrder == 0) {
            orderTopButton.setEnabled(false);
            orderTopButton.add(new AttributeAppender("class", " disabled"));
        }
        editorWrapper.add(orderTopButton);
        AjaxFallbackLink orderUpButton = new AjaxFallbackLink("btn_order_up") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(MoveListEventHandler handler : moveListEventHandlers) {
                    handler.handleMoveUp(ref.getDisplayOrder(), target);
                }
            }
        };
        orderUpButton.setVisible(!analysing);
        if(displayOrder == 0) {
            orderUpButton.setEnabled(false);
            orderUpButton.add(new AttributeAppender("class", " disabled"));
        }
        editorWrapper.add(orderUpButton);
        AjaxFallbackLink orderDownButton = new AjaxFallbackLink("btn_order_down") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(MoveListEventHandler handler : moveListEventHandlers) {
                    handler.handleMoveDown(ref.getDisplayOrder(), target);
                }
            }
        };
        orderDownButton.setVisible(!analysing);
        if(displayOrder == maxDisplayOrder) {
            orderDownButton.setEnabled(false);
            orderDownButton.add(new AttributeAppender("class", " disabled"));
        }
        editorWrapper.add(orderDownButton);
        AjaxFallbackLink orderBottomButton = new AjaxFallbackLink("btn_order_bottom") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(MoveListEventHandler handler : moveListEventHandlers) {
                    handler.handleMoveEnd(ref.getDisplayOrder(), target);
                }
            }
        };
        orderBottomButton.setVisible(!analysing);
        if(displayOrder == maxDisplayOrder) {
            orderBottomButton.setEnabled(false);
            orderBottomButton.add(new AttributeAppender("class", " disabled"));
        }
        editorWrapper.add(orderBottomButton);

        AjaxFallbackLink btnEdit = new AjaxFallbackLink("btn_edit") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(EventHandler handler : eventHandlers) {
                    handler.handleEditEvent(ref, target);
                }
            }
        };
        btnEdit.setEnabled(getButtonState(state));
        btnEdit.setVisible(!analysing);
        editorWrapper.add(btnEdit);
        
        AjaxFallbackLink btnRemove = new AjaxFallbackLink("btn_remove") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(EventHandler handler : eventHandlers) {
                    handler.handleRemoveEvent(ref, target);
                }
            }
        };
        btnRemove.setEnabled(getButtonState(state));
        btnRemove.setVisible(!analysing);
        editorWrapper.add(btnRemove);

        WebMarkupContainer spinner = new WebMarkupContainer("spinner");
        spinner.setVisible(analysing);
        editorWrapper.add(spinner);

        add(editorWrapper);
    }

    private boolean getButtonState(ReferencesEditor.State state) {
        return state != ReferencesEditor.State.INITIALIZED && state != ReferencesEditor.State.ANALYZING;
    }

    public void addEventHandler(EventHandler handler) {
        this.eventHandlers.add(handler);
    }

    public void addMoveListEventHandler(MoveListEventHandler handler) {
        this.moveListEventHandlers.add(handler);
    }
}
