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
 * Mock PID Metaresolver resolution requests and responses.
 * 
 * See: https://api.pidmr.devel.argo.grnet.gr/swagger-ui/
 *
 * @author wilelb
 */
public class PidmrApiMock {
    private final static Logger logger = LoggerFactory.getLogger(PidmrApiMock.class);
    
    private final MockWebServer server;
    private final ObjectMapper objectMapper;
    
    /**
     * Map with a list of registered pids. Use the registerPid() method to add
     * new pids.
     */
    private final Map<String, Pid> registeredPids = new HashMap<>();
    
    public PidmrApiMock() {
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
    
    public PidmrApiMock registerPid(String pid, String landingPageUrl, String metadataUrl, String resourceUrl) {
        if(!registeredPids.containsKey(pid)) {
            registeredPids.put(pid, new Pid(landingPageUrl, metadataUrl, resourceUrl));
        } else {
            Pid p = registeredPids.get(pid);
            p.setLandingPageUrl(landingPageUrl);
            p.setResourceUrl(resourceUrl);
            p.setMetadataUrl(metadataUrl);
            registeredPids.put(pid, p);
        }
        return this;
    }
    
    private void setupDispatcher() {
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                String method = request.getMethod();
                
                // Schema endpoints
                if (path.startsWith("/v1/metaresolvers/resolve")) {
                    return handleResolveRequests(request, path, method);
                }
                
                return new MockResponse().setResponseCode(404);
            }
        };
        server.setDispatcher(dispatcher);
    }
    
    private MockResponse handleResolveRequests(RecordedRequest request, String path, String method) {
        if (path.startsWith("/v1/metaresolvers/resolve") && "GET".equals(method)) {
            try {
                String pidValue = null;
                String pidModeValue = "landingpage";
                boolean redirectValue = false;
                
                //Parse query parameters
                String decodedPath = URLDecoder.decode(path, "UTF-8");
                Pattern regex = Pattern.compile("[\\?]?([\\w]+)=([\\w-\\:\\/]+)");
                Matcher m = regex.matcher(decodedPath);
                while(m.find()) {
                    String name = m.group(1);
                    String value = m.group(2);
                    if(null != name) switch (name) {
                        case "pid":
                            pidValue = value;
                            break;
                        case "pidMode":
                            pidModeValue = value;
                            break;
                        case "redirect":
                            redirectValue = Boolean.parseBoolean(value);
                            break;
                        default:
                            break;
                    }
                }
                
                return doResolve(pidValue, pidModeValue, redirectValue);
            } catch(UnsupportedEncodingException ex) {
                return new MockResponse().setResponseCode(400);
            }
        }

        return new MockResponse().setResponseCode(404);
    }
    
    private MockResponse doResolve(String pid, String pidMode, boolean redirect) {
        Pid p = registeredPids.get(pid);
        if(p == null) {
            return new MockResponse().setResponseCode(400);
        }
        
        String url = null;
        if(null != pidMode) switch(pidMode) {
            case "landingpage":
                url = p.getLandingPageUrl();
                break;
            case "metadata":
                url = p.getMetadataUrl();
                break;
            case "resource":
                url = p.getResourceUrl();
                break;
            default:
                url = null;
                break;
        }
        
        if(url == null) {
            return new MockResponse().setResponseCode(400);
        }
        
        try {
            if(redirect) {
                return new MockResponse()
                    .setResponseCode(302)
                    .setHeader("Location", url);
            }
            String json = objectMapper.writeValueAsString(new ResolutionResponse(url));
            return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(json);
        } catch (JsonProcessingException e) {
            return new MockResponse().setResponseCode(500);
        }
    }
    
    public class Pid {
        private String resourceUrl;
        private String landingPageUrl;
        private String metadataUrl;

        public Pid(String landingPageUrl, String metadataUrl, String resourceUrl) {
            this.resourceUrl = resourceUrl;
            this.metadataUrl = metadataUrl;
            this.landingPageUrl = landingPageUrl;
        }
        /**
         * @return the resourceUrl
         */
        public String getResourceUrl() {
            return resourceUrl;
        }

        /**
         * @param resourceUrl the resourceUrl to set
         */
        public void setResourceUrl(String resourceUrl) {
            this.resourceUrl = resourceUrl;
        }

        /**
         * @return the landingPageUrl
         */
        public String getLandingPageUrl() {
            return landingPageUrl;
        }

        /**
         * @param landingPageUrl the landingPageUrl to set
         */
        public void setLandingPageUrl(String landingPageUrl) {
            this.landingPageUrl = landingPageUrl;
        }

        /**
         * @return the metadataUrl
         */
        public String getMetadataUrl() {
            return metadataUrl;
        }

        /**
         * @param metadataUrl the metadataUrl to set
         */
        public void setMetadataUrl(String metadataUrl) {
            this.metadataUrl = metadataUrl;
        }
                
    }
}
