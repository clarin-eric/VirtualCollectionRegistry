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

    public final static String MODE_PRODUCTION = "prod";
    public final static String MODE_BETA = "beta";
    public final static String MODE_ALPHA = "alpha";

    @Value("${eu.clarin.cmdi.vcr.validators.http.timeout:5000}")
    private int httpTimeout;

    @Value("${eu.clarin.cmdi.vcr.validators.http.redirects:1}")
    private int httpRedirects;

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

    @Value("${eu.clarin.cmdi.vcr.mode:alpha}")
    private String mode;


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
    public String getMode() { return mode; }

    @Override
    public boolean isProductionMode() { return mode != null && mode.equalsIgnoreCase(MODE_PRODUCTION); }

    @Override
    public boolean isBetaMode() { return mode != null && mode.equalsIgnoreCase(MODE_BETA); }

    @Override
    public boolean isAlphaMode() { return mode != null && mode.equalsIgnoreCase(MODE_ALPHA); }

    @Override
    public String logConfig() {
            /*
        logger.info("Configuration:");
        logger.info("  logoutMode:             {}", logoutMode);
        logger.info("  logoutEnabled:          {}", logoutEnabled);
        logger.info("  locale:                 {}", locale);
        logger.info("  mode:                   {}", mode);
        logger.info("  forking enabled:        {}", forkingEnabled);
        logger.info("  Switchboard integration:");
        logger.info("    lrsEndpoint:          {}", lrsEndpoint);
        logger.info("    lrsEnableResources:   {}", lrsEnableResources);
        logger.info("    lrsEnableCollections: {}", lrsEnableCollections);
        logger.info("  Validators");
        logger.info("    http timeout:         {}", httpTimeout);
        logger.info("    http redirects:       {}", httpRedirects);

             */
        StringBuilder result = new StringBuilder();
        result.append("Configuration:\n");
        result.append("  logoutMode:             "+logoutMode+"\n");
        result.append("  logoutEnabled:          "+logoutEnabled+"\n");
        result.append("  locale:                 "+locale+"\n");
        result.append("  mode:                   "+mode+"\n");
        result.append("  forking enabled:        "+forkingEnabled+"\n");
        result.append("  Switchboard integration:\n");
        result.append("    lrsEndpoint:          "+lrsEndpoint+"\n");
        result.append("    lrsEnableResources:   "+lrsEnableResources+"\n");
        result.append("    lrsEnableCollections: "+lrsEnableCollections+"\n");
        result.append("  Validators\n");
        result.append("    http timeout:         "+httpTimeout+"\n");
        result.append("    http redirects:       "+httpRedirects+"\n");
        return result.toString();
    }

    @Override
    public int getHttpTimeout() {
        return httpTimeout;
    }

    @Override
    public int getHttpRedirects() {
        return httpRedirects;
    }
}
