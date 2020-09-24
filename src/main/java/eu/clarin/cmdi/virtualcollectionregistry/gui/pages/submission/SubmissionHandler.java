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
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.io.Serializable;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author wilelb
 */
public interface SubmissionHandler extends Serializable {
        public boolean checkVersion(String received_api_version);
        
        /**
         * Handle the incoming request and store all data as a collection in the 
         * session cache.
         * 
         * @param request
         * @param response
         * @param session
         * @param params
         * @param type 
         */
        public void handle(WebRequest request, WebResponse response, ApplicationSession session, PageParameters params , VirtualCollection.Type type);
        
        public void postProcess(VirtualCollection.Type type);
    
}
