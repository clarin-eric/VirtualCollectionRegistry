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
package eu.clarin.cmdi.vcr.rest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.clarin.cmdi.virtualcollectionregistry.core.jwt.JwtUtil;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.rest.VirtualCollectionXMLBodyWriter;
import eu.clarin.cmdi.virtualcollectionregistry.service.impl.QueryFactorySerializer;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class RequestBuilder {
    private final static String SERVICE_URL = "http://localhost:8080/service/v1";
    
    private WebTarget webTarget;
        
    private final JwtUtil jwtUtil;
    
    private Principal authenticatedUser;
    
    private final static Logger logger = LoggerFactory.getLogger(RequestBuilder.class);
    
    private RequestBuilder(WebTarget webTarget, JwtUtil jwtUtil) {
        this.webTarget = webTarget;
        this.jwtUtil = jwtUtil;
    }

    public static RequestBuilder setPath(String path, JwtUtil jwtUtil) {
        final Client x = ClientBuilder.newClient();
        x.register(VirtualCollectionXMLBodyWriter.class);
        RequestBuilder builder = new RequestBuilder(x.target(SERVICE_URL).path(path), jwtUtil);
        return builder;
    }

    public RequestBuilder setAuthenticatedUser(Principal authenticatedUser) {
        this. authenticatedUser = authenticatedUser;        
        return this;
    }
    
    public RequestBuilder addParam(String name, Object... values) {
        this.webTarget = webTarget.queryParam(name, values);
        return this;
    }
    
    public RequestBuilder addQueryFactory(QueryFactory factory) {
        try {
            String jsonEncodedAsBas64 = QueryFactorySerializer.serializeBase64Encoded(factory);
            this.webTarget = webTarget.queryParam("queryfactory", jsonEncodedAsBas64);
        } catch(JsonProcessingException ex) {
            logger.error("Failed to serialize QueryFactory as JSON", ex);
        }
        return this;
    }
    
    private Invocation.Builder getInvocationBuilder() {
        String uri = webTarget.getUri().toString();
        logger.debug("Build request: " + uri);
        Invocation.Builder 
            invocationBuilder = 
                webTarget
                    .request(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
        if(this.authenticatedUser != null) {
            String jwtToken = jwtUtil.createToken(this.authenticatedUser.getName());
            invocationBuilder.header("Authorization", "Bearer "+jwtToken);
        }
        
        return invocationBuilder;
    }

    public Response get() {
        return getInvocationBuilder().get();
    }

    public <T> T get(Class<T> responseType) {
        return getInvocationBuilder().get(responseType);
    }
    
    public <T> T get(GenericType<T> responseType) {
        return getInvocationBuilder().get(responseType);
    }
    
    public Response post(Entity entity) {
        return getInvocationBuilder().post(entity);
    }
    
    public Response put(Entity entity) {
        return getInvocationBuilder().put(entity);
    }
    
    public Response delete() {
        return getInvocationBuilder().delete();
    }
}
