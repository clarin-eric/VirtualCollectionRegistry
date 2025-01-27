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
package eu.clarin.pidmr.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author wilelb
 */
public class PidmrClientImpl implements PidmrClient {
    
    private final PidmrClientConfig config;
    
    public PidmrClientImpl(PidmrClientConfig config) {
        this.config = config;
    }
    
    @Override
    public String validatePid(String pid) throws PidmrException {
        final Client client = ClientBuilder.newClient();
        WebTarget webTarget = 
            client.target(config.getApiBaseUrl())
                .path("providers/validate")
                .queryParam("pid", pid);
        Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == 200) {
            String responseBody = response.readEntity(String.class);
            return responseBody;
        }
        
        throw new PidmrException("Unexpected response code " + response.getStatus());
    }
    @Override
    public String resolvePid(String pid) throws PidNotFoundException, PidmrException {
        final Client client = ClientBuilder.newClient();
        WebTarget webTarget = 
            client.target(config.getApiBaseUrl())
                .path("metaresolvers/resolve")
                .queryParam("pid", pid)
                .queryParam("pidMode", "landingpage")
                .queryParam("redirect", "false");
        Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == 200) {
            ResolutionResponse responseBody = response.readEntity(ResolutionResponse.class);
            return responseBody.getUrl();
        } else if(response.getStatus() == 404) {
            throw new PidNotFoundException(pid);
        }
        
        throw new PidmrException("Unexpected response code " + response.getStatus());
    }
    
}
