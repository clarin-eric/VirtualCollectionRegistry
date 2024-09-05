package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.AbstractDialogEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author wilelb
 */
public class ModalConfirmDialog extends Modal<String> {
    
    private static final long serialVersionUID = 4591006733317646110L;

    private final List<Listener> actionListeners = new ArrayList<>();
    
    private ModalConfirmAction action;
    
    private IModel<String> modalTitleModel;// = Model.of("");
    private IModel<String> modalTitleBody;// = Model.of("");
        
    public static String getContentId() {
        return "content";
    }
    
    public void addListener(Listener l) {
        actionListeners.add(l);
    }
    
    protected void fireEvent(Event evt) {
        for(Listener l : actionListeners) {
            l.handleEvent(evt);
        }
    }
    
    public void setModalConfirmAction(ModalConfirmAction action) {
        this.action = action;
        
    } 

    public ModalConfirmDialog(String markupId) {
        super(markupId, Model.of("Confirmation"));
        setOutputMarkupId(true);
        modalTitleBody = Model.of("");
        add(new BodyPanel(getContentId(), modalTitleBody));
        initFooter(this);
    }
     
    /**
     * @param markupId markup id
     * @param content
     */
    public ModalConfirmDialog(String markupId, Panel content) {
        super(markupId, Model.of("Confirmation"));  
        setOutputMarkupId(true);     
        modalTitleBody = Model.of("");
        add(content);
        initFooter(this);
    }
    
    protected void initFooter(final Modal modal) {
        AjaxFallbackLink<AjaxRequestTarget> buttonOk = new AjaxFallbackLink<>("button") {
            @Override
            public void onClick(final Optional<AjaxRequestTarget> target) {
                if(action != null) {
                    fireEvent(action.getEvent(target.get(), modal));
                } else {
                    fireEvent(new AbstractDialogEvent(EventType.OK, target.get(), modal));
                }
            }        
        };
        buttonOk.add(new AttributeModifier("class", "btn btn-primary"));
        buttonOk.add(new Label("button_label", Model.of("Ok")));                
        addButton(buttonOk);
        
        AjaxFallbackLink<AjaxRequestTarget> buttonCancel = new AjaxFallbackLink<>("button") {
            @Override
            public void onClick(final Optional<AjaxRequestTarget> target) {
                fireEvent(new AbstractDialogEvent(EventType.CANCEL, target.get(), modal));
            }        
        };
        buttonCancel.add(new AttributeModifier("class", "btn btn-default"));
        buttonCancel.add(new Label("button_label", Model.of("Cancel")));
        addButton(buttonCancel);
        
        setFooterVisible(true);
    }
    
     @Override
    protected Component createHeaderLabel(String id, String label) {
        if(modalTitleModel == null) {
            modalTitleModel = Model.of(label);
        } else {
            modalTitleModel.setObject(label);
        }
        return new Label(id, modalTitleModel);
    } 
    
    public void update(String title, String body) {
        this.modalTitleModel.setObject(title);
        this.modalTitleBody.setObject(body);
    }
    
    public class BodyPanel extends Panel {
        public BodyPanel(String id, IModel model) {
            super(id);
            add(new Label("message", model));
        }
    }
}
