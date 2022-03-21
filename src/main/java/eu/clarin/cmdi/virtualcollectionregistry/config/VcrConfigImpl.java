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

    public final static String ACTION_ENABLE_COLLECTIONS = "COLLECTIONS";
    public final static String ACTION_ENABLE_RESOURCES = "RESOURCES";

    @Value("${eu.clarin.cmdi.vcr.process.endpoint:https://switchboard.clarin.eu/#/vcr}")
    private String processEndpoint;

    @Value("${eu.clarin.cmdi.vcr.process.enable:RESOURCES}")
    private String processEnable;

    @Value("${eu.clarin.cmdi.vcr.process.prefered_pid_type:HDL}")
    private String processEndpointPreferedPidType;

    @Value("${eu.clarin.cmdi.vcr.process.popup:true}")
    private boolean processPopup;

    @Value("${eu.clarin.cmdi.vcr.download.endpoint:https://weblicht.sfs.uni-tuebingen.de/CMDIExplorer/#/vcr}")
    private String downloadEndpoint;

    @Value("${eu.clarin.cmdi.vcr.download.prefered_pid_type:HDL}")
    private String downloadEndpointPreferedPidType;

    @Value("${eu.clarin.cmdi.vcr.download.enable:COLLECTIONS}")
    private String downloadEnable;

    
    @Value("${eu.clarin.cmdi.vcr.logout_mode:basic}")
    private String logoutMode;
    
    @Value("${eu.clarin.cmdi.vcr.logout_enable:false}")
    private boolean logoutEnabled;
    
    @Value("${eu.clarin.cmdi.vcr.locale:en-GB}")
    private String locale;

    @Value("${eu.clarin.cmdi.vcr.forking.enabled:false}")
    private boolean forkingEnabled;

    private String getEndpointWithoutTrailingSlash(String endpoint) {
        if(endpoint == null) {
            return null;
        }
        if (endpoint.endsWith("/")) {
            return endpoint.substring(0, endpoint.length()-1);
        }
        return endpoint;
    }

    @Override
    public String getProcessEndpoint() { return getEndpointWithoutTrailingSlash(processEndpoint); }

    @Override
    public String getDownloadEndpoint() { return getEndpointWithoutTrailingSlash(downloadEndpoint); }

    private boolean hasConfig(String input, String config) {
        for(String s : input.split(",")) {
            if(s.trim().equalsIgnoreCase(config)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDownloadEndpointPreferedPidType() {return downloadEndpointPreferedPidType; }

    @Override
    public String getProcessEndpointPreferedPidType() {return processEndpointPreferedPidType; }

    @Override
    public boolean isProcessPopupEnabled() {
        return processPopup;
    }

    @Override
    public boolean isProcessEnabledForResources() { return hasConfig(processEnable, ACTION_ENABLE_RESOURCES); }

    @Override
    public boolean isProcessEnabledForCollections() { return hasConfig(processEnable, ACTION_ENABLE_COLLECTIONS); }

    @Override
    public boolean isDownloadEnabledForCollections() { return hasConfig(downloadEnable, ACTION_ENABLE_COLLECTIONS); }
    
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
        logger.info("  logoutMode:           {}", logoutMode);
        logger.info("  logoutEnabled:        {}", logoutEnabled);
        logger.info("  locale:               {}", locale);
        logger.info("  Process integration:");
        logger.info("    processEndpoint:    {}", processEndpoint);
        logger.info("    processEnable:      {}", processEnable);
        logger.info("  Download integration:");
        logger.info("    downloadEndpoint:   {}", downloadEndpoint);
        logger.info("    downloadEnable:     {}", downloadEnable);
    }
}
