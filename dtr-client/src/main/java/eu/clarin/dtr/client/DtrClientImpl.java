/*
 * Copyright (C) 2025 CLARIN
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
package eu.clarin.dtr.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

/**
 *
 * @author wilelb
 */
public class DtrClientImpl implements DtrClient {
    private final DtrClientConfig config;
    private final String apiBaseUrl;
    
    public DtrClientImpl(DtrClientConfig config) {
        this.config = config;
        this.apiBaseUrl = config.getScheme()+"://"+config.getHost()+(config.getPort() != null ? ":"+config.getPort() : "");
    }
    
    @Override
    public DtrType getType(String typeId) {
        final Client client = ClientBuilder.newClient(); 
        final WebTarget webTarget = client
            .target(apiBaseUrl)
            .path("objects/"+typeId);
        final DtrType response = webTarget
            .request(MediaType.APPLICATION_JSON)
            .get(DtrType.class);
        return response;
    }
    
    @Override
    public String getExtendedTypeCrosswalk(String typeId) {
        DtrType response = getType(typeId);
        for(DtrType.DtrReference ref : response.references) {
            if(ref.name.equalsIgnoreCase("CROSSWALK")) {
                return ref.url;
            }
        }
        throw new RuntimeException("No crosswalk reference found in DTR type references");
    }
}
