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
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author wilelb
 */
public abstract class AbstractSubmissionHandler implements SubmissionHandler {

    private final String version;
    
    public AbstractSubmissionHandler(String version) {
        this.version = version;
    }
    
    @Override
    public boolean checkVersion(String received_api_version) {
        return received_api_version.equalsIgnoreCase(version);
    }

    @Override
    public abstract void handle(WebRequest request, WebResponse response, ApplicationSession session, PageParameters params, VirtualCollection.Type type);

    @Override
    public abstract void postProcess(VirtualCollection.Type type);
}
