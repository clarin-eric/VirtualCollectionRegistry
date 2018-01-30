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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.markup.html.WebPage;

/**
 *
 * @author wilelb
 */
public class LogoutPage extends WebPage {

    public LogoutPage() {
        super();
        setStatelessHint(true);
        setVersioned(false);
    }

    @Override
    protected void onBeforeRender() {
        //TODO: how to implement logout?
        /*
        final RequestCycle cycle =  RequestCycle.get();
        final HttpServletRequest request = 
            (HttpServletRequest)cycle.getRequest().getContainerRequest();
        final HttpServletResponse response = 
            (HttpServletResponse)cycle.getResponse().getContainerResponse();
        if (request.getAuthType() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            final Principal principal = request.getUserPrincipal();
            ApplicationSession session = (ApplicationSession) getSession();
            if (session.signIn(principal)) {
                continueToOriginalDestination();
                // if we reach this line there was no intercept page, so go to home page
                throw new RestartResponseAtInterceptPageException(
                    Application.get().getHomePage());
            } else {
                throw new RestartResponseException(
                        Application.get().getApplicationSettings()
                            .getAccessDeniedPage());
            }
        }
*/
        super.onBeforeRender();
    }
    
}
