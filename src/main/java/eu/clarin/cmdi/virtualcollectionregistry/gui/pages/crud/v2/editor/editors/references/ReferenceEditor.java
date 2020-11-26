package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import com.sun.org.apache.xpath.internal.operations.Bool;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.CancelEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.SaveEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.VcrChoiceField;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.VcrTextArea;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.VcrTextField;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wilelb
 */
public class ReferenceEditor extends Panel {
    private final static Logger logger = LoggerFactory.getLogger(ReferenceEditor.class);
    
    private final IModel<String> urlModel = Model.of("");
    private final IModel<String> titleModel = Model.of("");
    private final IModel<String> descriptionModel = Model.of("");
    private final IModel<String> typeModel = Model.of(Resource.Type.RESOURCE.toString());

    private final Component componentToUpdate;
    
    private Resource data;
    
    public ReferenceEditor(String id, final Component componentToUpdate, final SaveEventHandler saveEventHandler, final CancelEventHandler cancelEventHandler, Model<Boolean> advancedEditor) {
        super(id); 
        final Component _this = this;
        this.componentToUpdate = componentToUpdate;
        
        WebMarkupContainer wrapper = new WebMarkupContainer("wrapper");

        VcrTextField tfUrl = new VcrTextField("url", "Url", "Reference url", urlModel);
        tfUrl.setCompleteSubmitOnUpdate(true);
        wrapper.add(tfUrl);

        VcrChoiceField typeField = new VcrChoiceField(
                "type",
                "Type",
                enumValuesAsList(Resource.Type.values()),
                typeModel);
        typeField.setCompleteSubmitOnUpdate(true);
        typeField.setVisible(advancedEditor.getObject());
        wrapper.add(typeField);

        VcrTextField tf = new VcrTextField("title", "Title", "", titleModel);
        tf.setCompleteSubmitOnUpdate(true);         
        wrapper.add(tf);
        
        VcrTextArea ta = new VcrTextArea("description", "Description", "", descriptionModel);
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

    private List<String> enumValuesAsList(Object[] values) {
        List<String> result = new ArrayList<>();
        for(Object o : values) {
            result.add(o.toString());
        }
        return result;
    }

    public void setReference(Resource ref) {
        data = ref;
        urlModel.setObject(ref.getRef());
        titleModel.setObject(ref.getLabel());
        descriptionModel.setObject(ref.getDescription());
        typeModel.setObject(ref.getType().toString());
    }

    public void reset() {
        urlModel.setObject("");
        titleModel.setObject("");
        descriptionModel.setObject("");
        typeModel.setObject(Resource.Type.RESOURCE.toString());
    }
}
