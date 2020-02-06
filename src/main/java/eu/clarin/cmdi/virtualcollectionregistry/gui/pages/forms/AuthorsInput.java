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

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
@SuppressWarnings("serial")
public class AuthorsInput extends FormComponentPanel<List<Creator>> {
    
    private static Logger logger = LoggerFactory.getLogger(AuthorsInput.class);
    
    final private IModel<String> personModel = Model.of("");
    final private IModel<String> emailModel = Model.of("");
    final private IModel<String> organisationModel = Model.of("");
    final private IModel<List<Creator>> listModel;

    public AuthorsInput(String id) {
        this(id, null);
    }

    public AuthorsInput(String id, IModel<List<Creator>> model) {
        super(id, model);	        
        this.listModel = model;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Form form = new Form("form");/* {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                
                logger.info("Authors form successfully submitted!");
                
                String person = personModel.getObject();
                String email = emailModel.getObject();
                String organisation = organisationModel.getObject();

                //logger.info("person: ["+person+"], email: ["+email+"], org: ["+organisation+"]");
                List<String> errors = new ArrayList<>();
                if (person == null || person.isEmpty()) {
                    errors.add("Person is a required field.");
                }

                if (errors.isEmpty()) {
                    Creator creator = new Creator(person);
                    if (email != null && !email.isEmpty()) {
                        creator.setEMail(email);
                    }
                    if (organisation != null && !organisation.isEmpty()) {
                        creator.setOrganisation(organisation);
                    }
                    
                    listModel.getObject().add(creator);
                    personModel.setObject(null);
                    emailModel.setObject(null);
                    organisationModel.setObject(null);
                }
            }

            @Override
            protected void onError() {
                logger.info("Authors form failed to validate!");
            }
        };*/
        form.setOutputMarkupId(true);

        Label lbl = new Label("label", "Author(s)");		
        TextField<String> inputPerson = new TextField("input_person", personModel);
        //inputPerson.setRequired(true);
        TextField<String> inputEmail = new TextField("input_email", emailModel);
        //inputEmail.setRequired(true);
        inputEmail.add(EmailAddressValidator.getInstance());
        TextField<String> inputOrganisation = new TextField("input_organisation", organisationModel);
        //Button btnAdd = new Button("btn_add", Model.of("Add"));
        AjaxSubmitLink btnAdd = new AjaxSubmitLink("btn_add", form) {//Model.of("Add"))  {                
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    
                    logger.info("Ajax submit");
                    
                   logger.info("Authors form successfully submitted!");
                
                    String person = personModel.getObject();
                    String email = emailModel.getObject();
                    String organisation = organisationModel.getObject();

                    //logger.info("person: ["+person+"], email: ["+email+"], org: ["+organisation+"]");
                    List<String> errors = new ArrayList<>();
                    if (person == null || person.isEmpty()) {
                        errors.add("Person is a required field.");
                    }

                    if (errors.isEmpty()) {
                        Creator creator = new Creator(person);
                        if (email != null && !email.isEmpty()) {
                            creator.setEMail(email);
                        }
                        if (organisation != null && !organisation.isEmpty()) {
                            creator.setOrganisation(organisation);
                        }

                        listModel.getObject().add(creator);
                        personModel.setObject(null);
                        emailModel.setObject(null);
                        organisationModel.setObject(null);
                    }
                    
                    target.add(form);
                    super.onSubmit(target, form);
                }
            };
        btnAdd.add(new AttributeModifier("class", "btn btn-default btn-xs"));
        
        ListView<Creator> listview = new ListView<Creator>("list", listModel) {
            @Override
            protected void populateItem(ListItem<Creator> item) {
                Creator c = item.getModelObject();
                item.add(new Label("person_label", c.getPerson()));
                item.add(new Label("email_label", c.getEMail()));
                item.add(new Label("organisation_label", c.getOrganisation()));

                AjaxLink btnRemove = new AjaxLink("item_remove", Model.of("(x)")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Creator creator = item.getModel().getObject();
                        logger.debug("Remove creator: "+creator);
                        listModel.getObject().remove(creator);
                        target.add(form);
                    }
                };
                btnRemove.add(new AttributeModifier("class", "btn btn-default btn-xs"));
                item.add(btnRemove);
            }
        };

        form.add(lbl);   
        form.add(inputPerson);
        form.add(inputEmail);
        form.add(inputOrganisation);
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
