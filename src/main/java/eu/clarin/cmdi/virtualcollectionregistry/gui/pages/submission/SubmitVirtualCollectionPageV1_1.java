/*
 * Copyright (C) 2020 CLARIN
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
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.CreateAndEditVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author wilelb
 */
public class SubmitVirtualCollectionPageV1_1 extends AbstractSubmissionHandler {
    
    public SubmitVirtualCollectionPageV1_1() {
        super("1.1");
    }
    
    @Override
    public void handle(WebRequest request, WebResponse response, ApplicationSession session, PageParameters params, VirtualCollection.Type type) {
        SubmissionUtils.checkSubmission(request, response, session, type);     
    }

    @Override
    public void postProcess(VirtualCollection.Type type) {
    }
}
