/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.config;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author wilelb
 */
@Component
public class VcrConfigImpl implements VcrConfig {
    private final static Logger logger = LoggerFactory.getLogger(VcrConfigImpl.class);
    
    @Value("${eu.clarin.cmdi.vcr.lrs.endpoint:https://switchboard.clarin.eu/#/vcr}")
    private String lrsEndpoint;
    
    @Value("${eu.clarin.cmdi.vcr.lrs.enable_for_resources:true}")
    private boolean lrsEnableResources;
    
    @Value("${eu.clarin.cmdi.vcr.lrs.enable_for_collections:false}")
    private boolean lrsEnableCollections;
    
    @Value("${eu.clarin.cmdi.vcr.logout_mode:basic}")
    private String logoutMode;
    
    @Value("${eu.clarin.cmdi.vcr.logout_enable:false}")
    private boolean logoutEnabled;
    
    @Value("${eu.clarin.cmdi.vcr.locale:en-GB}")
    private String locale;

    @Value("${eu.clarin.cmdi.vcr.forking.enabled:false}")
    private boolean forkingEnabled;

        @Override
    public String getSwitchboardEndpoint() {
        if (lrsEndpoint.endsWith("/")) {
            return lrsEndpoint.substring(0, lrsEndpoint.length()-1);
        }
        return lrsEndpoint;
    }
    
    @Override
    public boolean isSwitchboardEnabledForResources() {
        return lrsEnableResources;
    }
    
    @Override
    public boolean isSwitchboardEnabledForCollections() {
        return lrsEnableCollections;
    }
    
    @Override
    public boolean isLogoutEnabled() {
        return logoutEnabled;
    }
    
    
    @Override
    public String getLogoutMode() {
        return logoutMode;
    }
    
     @Override
    public String getLocaleString() {
        return locale;
    }
    
    @Override
    public Locale getLocale() {
        if(getLocaleString() == null) {
            logger.warn("No locale string available");
            return Locale.getDefault();
        }
        return new Locale(getLocaleString());
    }

    @Override
    public boolean isForkingEnabled() {
        return forkingEnabled;
    }

    @Override
    public void logConfig() {
        logger.info("Configuration:");
        logger.info("  lrsEndpoint:          {}", lrsEndpoint);
        logger.info("  lrsEnableResources:   {}", lrsEnableResources);
        logger.info("  lrsEnableCollections: {}", lrsEnableCollections);
        logger.info("  logoutMode:           {}", logoutMode);
        logger.info("  logoutEnabled:        {}", logoutEnabled);
        logger.info("  locale:               {}", locale);
    }
}
