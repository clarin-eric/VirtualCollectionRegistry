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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class DtrApiMock {
    private final static Logger logger = LoggerFactory.getLogger(DtrApiMock.class);
    
    private final MockWebServer server;
    private final ObjectMapper objectMapper;
    
    /**
     * Map with a list of registered pids. Use the registerPid() method to add
     * new pids.
     */
    private final Map<String, DtrType> registeredTypes = new HashMap<>();
    
      public DtrApiMock() {
        server = new MockWebServer();
        objectMapper = new ObjectMapper();
        setupDispatcher();
    }
    
     public void start(int port) throws IOException {
        server.start(port);
    }

    public void shutdown() throws IOException {
        server.shutdown();
    }

    public String getBaseUrl() {
        return server.url("/").toString();
    }
    
    public DtrApiMock registerType(String pid, DtrType type) {
        registeredTypes.put(pid, type);
        return this;
    }
    
    private void setupDispatcher() {
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                String method = request.getMethod();
                
                // Schema endpoints
                if (path.startsWith("/objects")) {
                    return handleResolveType(request, path, method);
                }
                
                return new MockResponse().setResponseCode(404);
            }
        };
        server.setDispatcher(dispatcher);
    }
    
     private MockResponse handleResolveType(RecordedRequest request, String path, String method) {
        if (path.startsWith("/objects") && "GET".equals(method)) {
            String pid = path.substring("/objects/".length(), path.length());
            
            if(!registeredTypes.containsKey(pid)) {
                return new MockResponse().setResponseCode(404);
            }
            
            try {
                DtrType type = registeredTypes.get(pid);
                String json = objectMapper.writeValueAsString(type);
                return new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(json);            
            } catch(JsonProcessingException ex) {
                logger.error("Failed to marshal type with id = "+pid+" as JSON", ex);
                return new MockResponse().setResponseCode(500);
            }
        }

        return new MockResponse().setResponseCode(404);
    }
}
