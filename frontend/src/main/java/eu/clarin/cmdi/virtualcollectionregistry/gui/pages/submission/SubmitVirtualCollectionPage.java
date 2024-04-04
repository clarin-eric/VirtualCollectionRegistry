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

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class SubmitVirtualCollectionPage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(SubmitVirtualCollectionPage.class);

    public class ErrorPanel extends Panel {
        public ErrorPanel(String id, String errorMessage) {
            super(id);
            add(new Label("error", errorMessage));
        }
    }

    public SubmitVirtualCollectionPage() {}

    @Override
    protected void onBeforeRender() {
        boolean hasPostParameters = SubmissionUtils.hasPostParameters((WebRequest)RequestCycle.get().getRequest());
        VirtualCollection cachedVc = SubmissionUtils.retrieveCollection(getSession());

        if(!hasPostParameters && cachedVc != null) {
            //No data submitted in request and collection loaded from cache, redirect to edit page to process cached data
            logger.info("Collection stored in session, redirect to edit page");
            throw new RestartResponseException(MergeCollectionsPage.class);
        }

        if(hasPostParameters && cachedVc != null) {
            //Data submitted in request and collection loaded from cache, clear cached data to allow processing of new data
            logger.info("Received new post data while collection was already cached. Clearing collection from cache.");
            SubmissionUtils.clearCollectionFromSession(getSession());
        }

        //No collection stored in session, process submitted data
        
        //Derivate type from page parameter
        String type_string = getPageParameters().get("type").toString();
        VirtualCollection.Type type = null;
        try {        
            type = VirtualCollection.Type.valueOf(type_string.toUpperCase());
        } catch(IllegalArgumentException ex) {
            //Invalid collection type
            //TODO: handle error
            logger.error("Invalid collection type: {}",type_string);
        }

        if (type != null) {
            String submissionError = SubmissionUtils.checkSubmission(
                    (WebRequest)RequestCycle.get().getRequest(),
                    (WebResponse)RequestCycle.get().getResponse(),
                    getSession(),
                    type
            );

            if(submissionError != null) {
                add(new Label("type", new Model(type.toString()+" Collection Submission")));
                add(new ErrorPanel("panel", "Submitted collection is not valid: "+submissionError));
            } else if(!isSignedIn()) {
                //Set proper content panel based on      
                add(new Label("type", new Model(type.toString()+" Collection Submission")));
                add(new LoginPanel("panel"));
            } else {
                //Already logged in, so redirect to creation page
                //TODO: show choice to add to an existing collection or create a new collection
                logger.info("Redirect logged in");
                throw new RestartResponseException(MergeCollectionsPage.class);
            }
        }

        //TODO: show error for invalid type?
        
        /** cascades the call to its children */
        super.onBeforeRender();
    }
}
