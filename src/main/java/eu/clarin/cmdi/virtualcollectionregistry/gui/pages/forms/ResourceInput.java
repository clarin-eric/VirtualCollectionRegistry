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

import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource.Type;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.validation.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
@SuppressWarnings("serial")
public class ResourceInput extends FormComponentPanel<List<Resource>> {
    
    private static Logger logger = LoggerFactory.getLogger(ResourceInput.class);
    
    final private IModel<TypeModel<Type>> typeModel = new Model(TypeModel.of(Type.RESOURCE));
    final private IModel<String> labelModel = Model.of("");
    final private IModel<String> referencesModel = Model.of("");
    final private IModel<String> descriptionModel = Model.of("");
    final private IModel<List<Resource>> listModel;

    public ResourceInput(String id) {
        super(id);
        this.listModel = new ListModel(new ArrayList<>());
    }

    public ResourceInput(String id, IModel<List<Resource>> model) {
        super(id, model);	
        this.listModel = model;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Form form = new Form("form") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                Type type = typeModel.getObject().getType();
                String reference = referencesModel.getObject();
                String label = labelModel.getObject();
                String description = descriptionModel.getObject();

                List<String> errors = new ArrayList<>();
                if (type == null) {
                    errors.add("Type is a required field.");
                }
                if (reference == null || reference.isEmpty()) {
                    errors.add("Reference is a required field.");
                }
                
                if (errors.isEmpty()) {
                    logger.info("Selected type: " + type);
                    Resource r = new Resource(type, reference);
                    if (label != null && !label.isEmpty()) {
                        r.setLabel(label);
                    }
                    if (description != null && !description.isEmpty()) {
                        r.setDescription(description);
                    }
                    
                    listModel.getObject().add(r);
                    typeModel.setObject(TypeModel.of(Type.RESOURCE));
                    referencesModel.setObject(null);
                    labelModel.setObject(null);
                    descriptionModel.setObject(null);
                }
            }

            @Override
            protected void onError() {
                logger.info("Form keywords failed to validate!");
            }
        };
        form.setOutputMarkupId(true);

        Label lbl = new Label("label", "Resource(s)");	
        
        
            
        final List<TypeModel<Type>> list = new ArrayList<>();
        for(Type t : Type.values()) {
            list.add(new TypeModel(t));
        }
                
        form.add(
            new CheckboxInput<>("input_type", typeModel, list)
            .setRequired(false)
        );
        TextField<String> inputReference = new TextField("input_reference", referencesModel);
        //inputReference.setRequired(true);
        TextField<String> inputLabel = new TextField("input_label", labelModel);        
        //inputReference.add(new UrlValidator(new String[]{"http://", "https://", "hdl://"}));
        TextArea<String> inputDescription = new TextArea("input_description", descriptionModel);
        Button btnAdd = new Button("btn_add", Model.of("Add"));
        btnAdd.add(new AttributeModifier("class", "btn btn-default btn-xs"));
        
        ListView<Resource> listview = new ListView<Resource>("list", listModel) {
            @Override
            protected void populateItem(ListItem<Resource> item) {
                Resource r = item.getModelObject();
                item.add(new Label("type_label", r.getType().name()));
                item.add(new Label("reference_label", r.getRef()));
                item.add(new Label("label_label", r.getLabel()));
                item.add(new Label("description_label", r.getDescription()));
                AjaxLink btnRemove = new AjaxLink("item_remove", Model.of("(x)")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Resource r = item.getModel().getObject();
                        logger.debug("Remove resource: "+r);
                        listModel.getObject().remove(r);
                        target.add(form);
                    }
                };
                btnRemove.add(new AttributeModifier("class", "btn btn-default btn-xs"));
                item.add(btnRemove);
            }
        };

        form.add(lbl);   
        form.add(inputReference);
        form.add(inputLabel);
        form.add(inputDescription);
        form.add(btnAdd);              
        form.add(listview);

        WebMarkupContainer wrapper = new WebMarkupContainer("wrapper");
        if(isRequired()) {
            wrapper.add(new AttributeAppender("class", " required"));
        }
        wrapper.add(form);
        add(wrapper);
    }
    
    @Override
    public void convertInput() {
        setConvertedInput(listModel.getObject());
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
    }    
    
    public static class TypeModel<T> implements Serializable {
        private final T type;       
        
        public static TypeModel of(Type t) {
            return new TypeModel(t);
        }
        
        public TypeModel(T type) {
            this.type = type;
        }
        
        public T getType() {
            return type;
        }
        
        @Override
        public boolean equals(Object o) {
            boolean result = false;            
            if (o instanceof TypeModel) {
                result = type.toString().equalsIgnoreCase(((TypeModel)o).toString());
            }
            return result;
        }
        
        @Override
        public String toString() {
            return this.type.toString();
        }
    }
}
