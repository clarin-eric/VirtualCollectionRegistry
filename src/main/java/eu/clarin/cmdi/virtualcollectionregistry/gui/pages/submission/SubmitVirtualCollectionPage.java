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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission;

import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.CreateAndEditVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author wilelb
 */
public class SubmitVirtualCollectionPage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(SubmitVirtualCollectionPage.class);
    private final SubmitHandlerFactory handlerFactory = new SubmitHandlerFactory();
    
    public SubmitVirtualCollectionPage() {}
    
    @Override
    protected void onBeforeRender() {     
        VirtualCollection vc = SubmissionUtils.retrieveCollection(getSession());
        if(vc != null) {        
            logger.info("Collection stored in session, redirect to edit page");
            throw new RestartResponseException(CreateAndEditVirtualCollectionPage.class);
        }
        
        logger.trace("No collection stored in session");     
        
        WebRequest request = (WebRequest)RequestCycle.get().getRequest();
        WebResponse response = (WebResponse)RequestCycle.get().getResponse();
        ApplicationSession session = getSession();
        PageParameters params = getPageParameters();
                
        //Derivate type from page parameter
        StringValue api_version = params.get("api_version");
        if(api_version.isEmpty()) {
            throw new SubmitVirtualCollectionException("API version is required");
        }
        
        StringValue type_string = params.get("type");
        if(type_string.isEmpty()) {
            throw new SubmitVirtualCollectionException("Collection type is required");
        }
        
        VirtualCollection.Type type = null;
        try {        
            type = VirtualCollection.Type.valueOf(type_string.toString().toUpperCase());
        } catch(IllegalArgumentException ex) {
             throw new SubmitVirtualCollectionException("Unsupported collection type: "+type_string);
        }
        
        boolean handled = false;
        for(SubmissionHandler handler : handlerFactory.getHandlers()) {
            if(handler.checkVersion(api_version.toString())) {
                handled = true;
                handler.handle(request, response, session, params, type);
            }
        }
                
        if(!handled) {
            throw new SubmitVirtualCollectionException("Unsupported API version: "+api_version.toString());
        } else {
            if(!isSignedIn()) {
                //Set proper content panel based on      
                add(new Label("type", new Model(type.toString())));
                add(new LoginPanel("panel"));
            } else {
                //Already logged in, so redirect to creation page
                //TODO: show choice to add to an existing collection or create a new collection
                logger.trace("Redirect logged in");
                throw new RestartResponseException(CreateAndEditVirtualCollectionPage.class);
            }
        }
 
        /** cascades the call to its children */
        super.onBeforeRender();
    }
}
