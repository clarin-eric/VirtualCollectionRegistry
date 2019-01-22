/*
 * Copyright (C) 2019 CLARIN
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

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class QueryInput extends FormComponentPanel {
    private static Logger logger = LoggerFactory.getLogger(QueryInput.class);
    private final IModel<String> descriptionModel = new Model<>("");
    private final IModel<String> labelModel = new Model<>("");
    private final IModel<String> profileModel = new Model<>("");
    private final IModel<String> valueModel = new Model<>("");
    
    public QueryInput(String id) {
        super(id);
        //this.listModel = new ListModel(new ArrayList<>());
    }
    
    public QueryInput(String id, IModel model) {
        super(id, model);	
        //this.listModel = model;
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        Form form = new Form("form") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                
                logger.info("Description: {}, Label: {}, Profile: {}, Value: {}", 
                        descriptionModel.getObject(), labelModel.getObject(), 
                        profileModel.getObject(), valueModel.getObject());
            }

            @Override
            protected void onError() {
                logger.info("Form keywords failed to validate!");
            }
        };
        form.setOutputMarkupId(true);
        
        form.add(new Label("qry_desc_label", "Description"));       
        TextArea<String> inputDescription = new TextArea("qry_desc_input", descriptionModel);
        form.add(inputDescription);
        
        form.add(new Label("qry_uri_label", "URI"));
        TextField<String> inputUri = new TextField("qry_uri_input", labelModel);        
        form.add(inputUri);
        
        form.add(new Label("qry_profile_label", "Query profile"));
        TextField<String> inputProfile = new TextField("qry_profile_input", profileModel);        
        form.add(inputProfile);
        
        form.add(new Label("qry_value_label", "Query value"));
        TextArea<String> inputValue = new TextArea("qry_value_input", valueModel);
        form.add(inputValue);
        
        Label lbl = new Label("label", "Collection Query");	     
        form.add(lbl);  
        
        WebMarkupContainer wrapper = new WebMarkupContainer("wrapper");
        if(isRequired()) {
            wrapper.add(new AttributeAppender("class", " required"));
        }
        wrapper.add(form);
        add(wrapper);
        
    }
    
    @Override
    public void convertInput() {
        //setConvertedInput(listModel.getObject());
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
    }    
}
