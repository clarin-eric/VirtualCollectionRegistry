package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.ReferenceParserResult;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.EventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.MoveListEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan.State;
import static eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan.State.DONE;
import static eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan.State.FAILED;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PidLink;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
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
     * @param ref   The actual reference as stored in the collection
     * @param scan  The scan result 
     * @param state 
     * @param reason 
     * @param advancedEditorMode 
     * @param maxDisplayOrder 
     * @param refReasonCollapseState 
     * @param rescanHandler 
     */

    public ReferencePanel(String id, final Resource ref, final ResourceScan scan, 
            final State state, String reason, 
            Model<Boolean> advancedEditorMode, long maxDisplayOrder, 
            Map<String, Boolean> refReasonCollapseState, ReferencesEditor.RescanHandler rescanHandler) {
        super(id);
        
        boolean reasonCollapsed = true;
        if(refReasonCollapseState.containsKey(ref.getRef())) {
            reasonCollapsed = refReasonCollapseState.get(ref.getRef());
        }

        long displayOrder = ref.getDisplayOrder();

        Model titleModel = Model.of("<required>");
        if(ref.getLabel() != null && !ref.getLabel().isEmpty()) {
            titleModel.setObject(ref.getLabel());
        } else if(scan != null) {
            String value = scan.getResourceScanLogLastValue(ReferenceParserResult.KEY_NAME);
            if(value != null) {
                titleModel.setObject(value);
            }
        }
        Model descriptionModel = Model.of("<required>");
        if(ref.getDescription() != null && !ref.getDescription().isEmpty()) {
            descriptionModel.setObject(ref.getDescription());
        } else if(scan != null) {

            String value = scan.getResourceScanLogLastValue(ReferenceParserResult.KEY_DESCRIPTION);
            if(value != null) {
                descriptionModel.setObject(value);
            }
            
        }
        
        //todo: list other kv pairs from scanner?

        /*
        WebMarkupContainer divReason = new WebMarkupContainer("reason");
        divReason.setOutputMarkupId(true);
        AjaxFallbackLink reasonBtnRescan = new AjaxFallbackLink("btn_rescan") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                rescanHandler.rescan(ref.getRef(), target);
            }
        };
        divReason.add(reasonBtnRescan);
        */
        
        /*
        Label reasonLbl = new Label("reason_lbl", reason);
        reasonLbl.setEscapeModelStrings(false);
        if(state == State.FAILED) {
            reasonLbl.add(new AttributeModifier("class", "icon-failed"));
        } else if(reasonCollapsed) {
            divReason.add(new AttributeModifier("class", "collapse"));
        }
        divReason.add(reasonLbl);
        */
        
        final Panel pnlScanDetails = new ReferenceScanDetails("scan_details", scan, rescanHandler);
        pnlScanDetails.setOutputMarkupId(true);
        if(reasonCollapsed) {
            pnlScanDetails.add(new AttributeModifier("class", "collapse"));
        }
        //divReason.add(pnlScanDetails);
        
        WebMarkupContainer editorWrapper = new WebMarkupContainer("wrapper");
        //editorWrapper.add(divReason);
        editorWrapper.add(pnlScanDetails);

        WebMarkupContainer stateIcon = new WebMarkupContainer("state");
        if(reason != null && !reason.isEmpty()) {
            UIUtils.addTooltip(stateIcon, reason);
        }
        switch(state) {
            case DONE:
                if(PidLink.isSupportedPersistentIdentifier(ref.getRef())) {
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

        AjaxFallbackLink reasonToggleLink = new AjaxFallbackLink("reason_toggle_btn") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if(state != State.FAILED) {
                    if (refReasonCollapseState.containsKey(ref.getRef())) {
                        refReasonCollapseState.put(ref.getRef(), !refReasonCollapseState.get(ref.getRef()));
                    } else {
                        refReasonCollapseState.put(ref.getRef(), false);
                    }
                }

                if(target != null) {

                }
            }
        };
        reasonToggleLink.setEnabled(state != State.FAILED);
        reasonToggleLink.add(new AttributeModifier("data-toggle","collapse"));
        reasonToggleLink.add(new AttributeModifier("data-target","#"+pnlScanDetails.getMarkupId()));
        reasonToggleLink.add(stateIcon);

        editorWrapper.add(reasonToggleLink);

        String urlValue = ref.getRef();
        if(!titleModel.getObject().toString().isEmpty()) {
            urlValue = "("+ref.getRef()+")";
        }
        editorWrapper.add(new Label("value", urlValue));

        Label lblWaiting = new Label("lbl_waiting", "Waiting on analysis");
        lblWaiting.setVisible(ResourceScan.isStateAnalyzing(state));
        editorWrapper.add(lblWaiting);

        Label lblTypeLabel = new Label("lbl_type", "Type:");
        lblTypeLabel.setVisible(!ResourceScan.isStateAnalyzing(state) && advancedEditorMode.getObject());
        editorWrapper.add(lblTypeLabel);
        Label lblType = new Label("type", ref.getType());
        lblType.setVisible(!ResourceScan.isStateAnalyzing(state) && advancedEditorMode.getObject());
        editorWrapper.add(lblType);

        Label lblTitle = new Label("title", titleModel);
        lblTitle.setVisible(!ResourceScan.isStateAnalyzing(state));
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
        labelDescription.setVisible(!ResourceScan.isStateAnalyzing(state));
        editorWrapper.add(labelDescription);

        AjaxFallbackLink orderTopButton = new AjaxFallbackLink("btn_order_top") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(MoveListEventHandler handler : moveListEventHandlers) {
                    handler.handleMoveTop(ref.getDisplayOrder(), target);
                }
            }
        };
        orderTopButton.setVisible(!ResourceScan.isStateAnalyzing(state));
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
        orderUpButton.setVisible(!ResourceScan.isStateAnalyzing(state));
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
        orderDownButton.setVisible(!ResourceScan.isStateAnalyzing(state));
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
        orderBottomButton.setVisible(!ResourceScan.isStateAnalyzing(state));
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
        btnEdit.setEnabled(!ResourceScan.isStateAnalyzing(state));
        btnEdit.setVisible(!ResourceScan.isStateAnalyzing(state));
        editorWrapper.add(btnEdit);
        
        AjaxFallbackLink btnRemove = new AjaxFallbackLink("btn_remove") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(EventHandler handler : eventHandlers) {
                    handler.handleRemoveEvent(ref, target);
                }
            }
        };
        btnRemove.setEnabled(!ResourceScan.isStateAnalyzing(state));
        btnRemove.setVisible(!ResourceScan.isStateAnalyzing(state));
        editorWrapper.add(btnRemove);

        WebMarkupContainer spinner = new WebMarkupContainer("spinner");
        spinner.setVisible(ResourceScan.isStateAnalyzing(state));
        //TODO: set different icons for initializing, queued and analyzing states
        
        Model<String> spinnerLabelModel = Model.of("Initialised");
        if(state == State.QUEUED) {
            spinnerLabelModel = Model.of("Queued");
        } else if(state == State.ANALYZING) {
            spinnerLabelModel = Model.of("Analysing");
        }
        WebMarkupContainer spinnerIcon = new WebMarkupContainer("spinner_icon");
        spinnerIcon.add(new AttributeAppender("class",
                new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        String cssClass = "";
                        //if(state == State.INITIALIZED) {
                        //    cssClass = "glyphicon glyphicon-stop";
                        //} else if(state == State.QUEUED) {
                        //    cssClass = "glyphicon glyphicon-pause";
                        //} else if(state == State.ANALYZING) {
                            cssClass = "lds-dual-ring";
                        //}
                        return cssClass;
                    }
                }, " "));
        
        spinner.add(spinnerIcon);
        spinner.add(new Label("spinner_text", spinnerLabelModel));
        editorWrapper.add(spinner);

        add(editorWrapper);
    }

    public void addEventHandler(EventHandler handler) {
        this.eventHandlers.add(handler);
    }

    public void addMoveListEventHandler(MoveListEventHandler handler) {
        this.moveListEventHandlers.add(handler);
    }
}
