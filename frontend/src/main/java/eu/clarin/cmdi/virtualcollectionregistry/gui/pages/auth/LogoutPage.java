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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth;

import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfigImpl;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class LogoutPage extends WebPage {

    private static Logger logger = LoggerFactory.getLogger(LogoutPage.class);
    
    @SpringBean
    private VcrConfigImpl vcrConfig;
    
    public LogoutPage() {
        super();
        setStatelessHint(true);
        setVersioned(false);
    }

    @Override
    protected void onBeforeRender() {
        if (vcrConfig.getLogoutMode().equalsIgnoreCase("basic")) {
            AuthenticationHandler.handleBasicLogout((ApplicationSession) getSession(), this);
        } else if (vcrConfig.getLogoutMode().equalsIgnoreCase("shibboleth")) {
            AuthenticationHandler.handleShibbolethLogout((ApplicationSession) getSession(), this);
        } else {
            logger.error("Unsuported logout mode: "+vcrConfig.getLogoutMode());
        }
        super.onBeforeRender();
    }
    
}
