/*
 * Copyright (C) 2017 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.radio.AjaxBootstrapRadioGroup;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.radio.BootstrapRadioGroup;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.radio.EnumRadioChoiceRenderer;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.radio.IRadioChoiceRenderer;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.UIUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * references: 
 *  * https://cwiki.apache.org/confluence/display/WICKET/Using+RadioGroups
 *  * https://blog.55minutes.com/2011/10/how-to-implement-radio-buttons-in-wicket/
 * 
 * @author wilelb
 * @param <T>
 */
@SuppressWarnings("serial")
public class CheckboxInput<T extends Enum<T>> extends FormComponentPanel<T> {
    
    private static Logger logger = LoggerFactory.getLogger(CheckboxInput.class);
    
    private final IModel<T> model;     //current value
    private final List<T> values;      //list of allowed values
    private final String labelText;    //Tekst for the display label
    private final String tooltipText;  //Tekst for the tooltip
    private final String tooltipViewport;
    private final String tooltipPlacement;
    
    //private RadioGroup<T> group;
    private BootstrapRadioGroup<T> group;
    
    private transient CheckboxInputChangeListener listener;
    
    public CheckboxInput(String id, IModel<T> model, List<T> values) {
        this(id, model, values, null, null, null, null);
    }
    
    /**
     * Used by extenstions.
     * @param id
     * @param label         Optional, can be null   
     * @param tooltipText   Optional, can be null
     * @param model
     * @param values
     * @param tooltipViewport
     * @param tooltipPlacement
     */
    public CheckboxInput(String id, IModel<T> model, List<T> values, String label, String tooltipText, String tooltipViewport, String tooltipPlacement) {
        super(id, model);
        this.model = model;
        this.values = values;
        this.labelText = label;
        this.tooltipText = tooltipText;
        this.tooltipPlacement = tooltipPlacement;
        this.tooltipViewport = tooltipViewport;
    }
    
    public void setCheckboxInputChangeListener (CheckboxInputChangeListener listener) {
    this.listener = listener;
}

    @Override
    protected void onModelChanged() {
        super.onModelChanged(); //To change body of generated methods, choose Tools | Templates.
        logger.info("CheckboxInput model changed: "+this.model.toString());
    }

    private class EnumRadioGroup<T extends Serializable> extends AjaxBootstrapRadioGroup<T> {

        public EnumRadioGroup(String id, Collection<T> options) {
            super(id, options);
        }   

        public EnumRadioGroup(String id, Collection<T> options, IRadioChoiceRenderer<T> choiceRenderer) {
            super(id, options, choiceRenderer);
        }

        public EnumRadioGroup(String id, IModel<T> model, Collection<T> options, IRadioChoiceRenderer<T> choiceRenderer) {
            super(id, model, options, choiceRenderer);
        }
    
        @Override
        protected void onSelectionChanged(AjaxRequestTarget art, Serializable t) {
            if (listener != null) listener.handleEvent(art, model);
        }
        
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();        
        
        //group = new BootstrapRadioGroup<>("group", model, values, new EnumRadioChoiceRenderer(Buttons.Type.Primary));
        
        group = new EnumRadioGroup<>("group", model, values, new EnumRadioChoiceRenderer(Buttons.Type.Primary));
        group.add(new AttributeAppender("class", " btngroup-spacing"));
        group.setOutputMarkupPlaceholderTag(true);
        
        UIUtils.addTooltip(group, tooltipText, tooltipViewport, "right");//tooltipPlacement);
        //WebMarkupContainer tooltip = new WebMarkupContainer("tooltipwrapper");
        //UIUtils.addTooltip(tooltip, tooltipText, tooltipViewport, tooltipPlacement);
        //tooltip.add(group);
        
        WebMarkupContainer container = new WebMarkupContainer("row");
        if(isRequired()) {
            container.add(new AttributeAppender("class", " required"));
        }
        if (this.labelText != null) {
            container.add(new Label("label", this.labelText));
        } else {
            
            container.add(new Label("label", this.labelText).setVisible(false));
        }
        container.add(group);
        add(container);
    }

    @Override
    public void convertInput() {        
        //logger.trace("Model object: "+model.getObject()+", list model object: "+group.getModelObject());
        setConvertedInput(group.getModelObject());
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        logger.debug("Model object: "+model.getObject()+", list model object: "+group.getModelObject());
    }  
    
    
}
