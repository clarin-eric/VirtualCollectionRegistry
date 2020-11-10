package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.CancelEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.SaveEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.VcrTextArea;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.VcrTextField;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class ReferenceEditor extends Panel {
    private final static Logger logger = LoggerFactory.getLogger(ReferenceEditor.class);
    
    private final IModel<String> urlModel = Model.of("");
    private final IModel<String> titleModel = Model.of("");
    private final IModel<String> descriptionModel = Model.of("");
    
    private final Component componentToUpdate;
    
    private Resource data;
    
    public ReferenceEditor(String id, final Component componentToUpdate, final SaveEventHandler saveEventHandler, final CancelEventHandler cancelEventHandler) {
        super(id); 
        final Component _this = this;
        this.componentToUpdate = componentToUpdate;
        
        add(new Label("url", urlModel));
        
        WebMarkupContainer wrapper = new WebMarkupContainer("wrapper");
        
        VcrTextField tf = new VcrTextField("title", "Title", "", titleModel);//, this);
        tf.setCompleteSubmitOnUpdate(true);         
        wrapper.add(tf);
        
        VcrTextArea ta = new VcrTextArea("description", "Description", "", descriptionModel);//, this);
        ta.setCompleteSubmitOnUpdate(true); 
        wrapper.add(ta);
        
        wrapper.add(new AjaxFallbackLink("save") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                data.setLabel(titleModel.getObject());
                data.setDescription( descriptionModel.getObject());
                reset();
                saveEventHandler.handleSaveEvent();
                if (target != null) {
                    target.add(componentToUpdate);
                }
            }
        });
        wrapper.add(new AjaxFallbackLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                reset();
                cancelEventHandler.handleCancelEvent();
                if (target != null) {
                    target.add(componentToUpdate);
                }
            }
        });
        
        add(wrapper);
    }
    
    public void setReference(Resource ref) {
        data = ref;
        urlModel.setObject(ref.getRef());
        titleModel.setObject(ref.getLabel());
        descriptionModel.setObject(ref.getDescription());
    }

    public void reset() {
        urlModel.setObject("");
        titleModel.setObject("");
        descriptionModel.setObject("");
    }
}
