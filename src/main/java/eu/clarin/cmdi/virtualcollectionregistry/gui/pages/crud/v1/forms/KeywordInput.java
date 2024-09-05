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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v1.forms;

import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
@SuppressWarnings("serial")
public class KeywordInput extends FormComponentPanel<List<String>> {
    
        private static Logger logger = LoggerFactory.getLogger(KeywordInput.class);
    
        private IModel<String> editorModel = Model.of("");
        private IModel<List<String>> listModel;
        
	public KeywordInput(String id) {
            super(id);		
	}

	public KeywordInput(String id, IModel<List<String>> model) {
            super(id, model);	
            this.listModel = model;
            logger.info("Items in model = {}", model.getObject().size());
	}

	@Override
	protected void onInitialize() {
            super.onInitialize();

            Form form = new Form("form");
            form.setOutputMarkupId(true);
            form.add(new AjaxFormSubmitBehavior("submit") {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    logger.info("On Submit Behavior");
                    super.onSubmit(target); // Breakpoint on this line
                    
                }
            });

            Label lbl = new Label("label", "Keywords");		
            TextField<String> editor = new TextField("editor", editorModel);
            
            AjaxButton btnAdd = new AjaxButton("btn_add", form) {//Model.of("Add"))  {                
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    
                    logger.info("Ajax submit");
                    
                    String keyword = editorModel.getObject();
                    if(keyword != null && !keyword.isEmpty()) {
                        logger.debug("Keyword = " + keyword + ", #Keywods = " + listModel.getObject().size());                        
                        listModel.getObject().add(keyword);
                        editorModel.setObject("");
                    }
                    
                    target.add(form);
                    super.onSubmit(target);
                }
            };
            btnAdd.add(new AttributeAppender("class", "btn btn-default btn-xs"));

            ListView<String> listview = new ListView<String>("list", listModel) {
                @Override
                protected void populateItem(ListItem<String> item) {
                    Label lbl = new Label("item_label", item.getModel().getObject());
                    AjaxLink btnRemove = new AjaxLink("item_remove", Model.of("(x)")) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            String keyword = item.getModel().getObject();
                            logger.debug("Remove keyword: "+keyword);
                            listModel.getObject().remove(keyword);
                            target.add(form);
                        }
                    };
                    btnRemove.add(new AttributeAppender("class", "btn btn-default btn-xs"));
                    item.add(lbl);   
                    item.add(btnRemove);
                }
            };

            form.add(lbl);   
            form.add(editor);
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
    
}
