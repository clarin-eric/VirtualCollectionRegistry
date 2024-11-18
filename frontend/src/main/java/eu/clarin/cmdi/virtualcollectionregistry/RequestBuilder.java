/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author wilelb
 */
public class RequestBuilder {
    private final static String SERVICE_URL = "http://localhost:8080/service";
    
    private final WebTarget webTarget;
        
    private RequestBuilder(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    public static RequestBuilder setPath(String path) {
        final Client x = ClientBuilder.newClient();
        RequestBuilder builder = new RequestBuilder(x.target(SERVICE_URL).path(path));
        return builder;
    }

    public RequestBuilder addParam(String name, Object... values) {
        webTarget.queryParam(name, values);
        return this;
    }

    public Response get() {
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        return invocationBuilder.get();
    }

    public <T> T get(Class<T> responseType) {
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        return invocationBuilder.get(responseType);
    }
    
    public <T> T get(GenericType<T> responseType) {
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        return invocationBuilder.get(responseType);
    }
    
    public Response post(Entity entity) {
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        return invocationBuilder.post(entity);
    }
}
