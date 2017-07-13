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
package eu.clarin.cmdi.virtualcollectionregistry.gui.forms;

import java.io.Serializable;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
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
public class CheckboxInput<T extends Serializable> extends FormComponentPanel<T> {
    
    private static Logger logger = LoggerFactory.getLogger(CheckboxInput.class);
    
    private final IModel<T> model;     //current value
    private final List<T> values;      //list of allowed values
    private final String labelText;         //Tekst for the display label
    private final String tooltipText;       //Tekst for the tooltip
    
    private RadioGroup<T> group;
    
    public CheckboxInput(String id, IModel<T> model, List<T> values) {
        this(id, model, values, null, null);
    }
    
    /**
     * Used by extenstions.
     * @param id
     * @param label         Optional, can be null   
     * @param tooltipText   Optional, can be null
     * @param model
     * @param values
     */
    public CheckboxInput(String id, IModel<T> model, List<T> values, String label, String tooltipText) {
        super(id, model);
        this.model = model;
        this.values = values;
        this.labelText = label;
        this.tooltipText = tooltipText;
    }

   @Override
    protected void onInitialize() {
        super.onInitialize();        
        
        ListView<T> listView = new ListView<T>("list", this.values) {
            @Override
            protected void populateItem(ListItem<T> item) {
                /*
                WebMarkupContainer wrapper = new WebMarkupContainer("input_wrapper");
                wrapper.setOutputMarkupId(true);
                if (item.getModelObject().equals(model.getObject())) {
                    wrapper.add(new AttributeModifier("class", "btn btn-primary btn-xs active"));        
                } else {
                    wrapper.add(new AttributeModifier("class", "btn btn-primary btn-xs"));
                }
                */
                item.add(
                    new Radio("input", item.getModel())
                    .add(new AttributeModifier("class", "btn btn-primary btn-xs")));
                item.add(
                    new Label("input_label", item.getModel()));
            }
        };
        
        this.group = new RadioGroup<>("group", new Model<>(this.model.getObject()));
        group.add( new AjaxFormChoiceComponentUpdatingBehavior() { 
            @Override
            protected void onUpdate(AjaxRequestTarget target) {                                        
                model.setObject(group.getModelObject());
                convertInput();
            }
        });                
        group.add(listView);
        
        WebMarkupContainer tooltip = new WebMarkupContainer("tooltipwrapper");
        tooltip.add(group);
        
        WebMarkupContainer container = new WebMarkupContainer("row");
        if(isRequired()) {
            container.add(new AttributeAppender("class", " required"));
        }
        if (this.labelText != null) {
            container.add(new Label("label", this.labelText));
        } else {
            
            container.add(new Label("label", this.labelText).setVisible(false));
        }
        container.add(tooltip);
        add(container);
    }

    @Override
    public void convertInput() {        
        logger.trace("Model object: "+model.getObject()+", list model object: "+group.getModelObject());
        setConvertedInput(group.getModelObject());
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        logger.trace("Model object: "+model.getObject()+", list model object: "+group.getModelObject());
    }        
}
