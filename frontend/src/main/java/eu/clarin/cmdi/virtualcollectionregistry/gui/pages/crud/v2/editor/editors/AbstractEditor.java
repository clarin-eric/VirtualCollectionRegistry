package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public abstract class AbstractEditor extends Panel {
    
    private static Logger logger = LoggerFactory.getLogger(AbstractEditor.class);
    
    public AbstractEditor(String id) {
        super(id);
    }
    
    protected TextField addField(final String idSuffix, final IModel labelModel, final IModel dataModel) {
         add(new Label("lbl_"+idSuffix, labelModel));

        final TextField tf = new TextField("input_"+idSuffix, dataModel);
        tf.add(new AjaxFormComponentUpdatingBehavior("blur") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                logger.trace("onUpdate: triggered via onBlur");
                handleUpdateData(target, dataModel, null);
            }
        });
        tf.add(new AjaxFormComponentOnKeySubmitBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {               
                logger.trace("onUpdate: triggered via keyPress, jsKeycode=[" + getPressedKeyCode() + "]");
                if(pressedReturn()) {
                    logger.trace("onUpdate: pressedReturn() == true");
                    handleUpdateData(target, dataModel, null);
                }
            }
        });
        add(tf);
        
        return tf;
    }
    
    protected abstract void handleUpdateData(AjaxRequestTarget target, IModel modelToUpdate, Component nextComponentToFocus);
}
