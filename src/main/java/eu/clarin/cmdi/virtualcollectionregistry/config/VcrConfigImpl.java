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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author wilelb
 */
@Component
public class VcrConfigImpl implements VcrConfig {
    @Value("${eu.clarin.cmdi.vcr.lrs.endpoint:https://switchboard.clarin.eu/#/vcr}")
    private String lrsEndpoint;
    
    @Value("${eu.clarin.cmdi.vcr.lrs.enable_for_resources:true}")
    private boolean lrsEnableResources;
    
    @Value("${eu.clarin.cmdi.vcr.lrs.enable_for_collections:false}")
    private boolean lrsEnableCollections;
    
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
}
