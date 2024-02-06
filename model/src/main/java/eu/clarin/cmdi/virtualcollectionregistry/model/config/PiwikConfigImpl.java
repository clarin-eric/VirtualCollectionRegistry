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
package eu.clarin.cmdi.virtualcollectionregistry.model.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration for communication with the Piwik instance
 * 
 * @see https://github.com/clarin-eric/VLO/blob/master/vlo-web-app/src/main/java/eu/clarin/cmdi/vlo/config/PiwikConfig.java
 * 
 * @author wilelb
 */
@Component
public class PiwikConfigImpl implements PiwikConfig {

    @Value("${eu.clarin.cmdi.vcr.piwik.enableTracker:true}")
    private boolean enabled;
    
    @Value("${eu.clarin.cmdi.vcr.piwik.siteId:6}")
    private String piwikSiteId;

    @Value("${eu.clarin.cmdi.vcr.piwik.host:https://stats.clarin.eu/}")
    private String piwikHost;

    @Value("${eu.clarin.cmdi.vcr.piwik.domains:*.vcr.clarin.eu}")
    private String domains;
    
    @Value("${eu.clarin.cmdi.vcr.snippet.survey:survey.html}")
    private String snippetSurvey;
 
    @Value("${eu.clarin.cmdi.vcr.snippet.credits:credits.html}")
    private String snippetCredits;
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getSiteId() {
        return piwikSiteId;
    }

    @Override
    public String getPiwikHost() {
        return piwikHost;
    }

    @Override
    public String getDomains() {
        return domains;
    }

    @Override
    public String getSnippetSurvey() {
        return snippetSurvey;
    }

    @Override
    public String getSnippetCredits() {
        return snippetCredits;
    }
}
