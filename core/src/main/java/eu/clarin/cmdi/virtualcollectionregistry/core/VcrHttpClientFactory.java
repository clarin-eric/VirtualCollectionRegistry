/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage a static reference to the PoolingHttpClientConnectionManager. Any class
 * needing a http client should get it from this factory. A special listener is
 * used to ensure proper cleanup on undeploy of the webapp (See VcrRestApplicationCleanupListener).
 * 
 * @author wilelb
 */
public class VcrHttpClientFactory {
    private final static Logger logger = LoggerFactory.getLogger(VcrHttpClientFactory.class);
    
    private static PoolingHttpClientConnectionManager connManager;

    //Track all clients we create so we can make sure they are all properly closed upon shutdown
    private final static List<CloseableHttpClient> clients = new ArrayList<>();
    
    public static synchronized CloseableHttpClient getClient() {
        if(connManager == null) {
            connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(100);
            connManager.setDefaultMaxPerRoute(20);
        }
        
        
        CloseableHttpClient client = 
            HttpClients.custom()
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .build();
        clients.add(client);
        return client;
            
    }

    public static synchronized CloseableHttpClient getNonRedirectClient() {
        if(connManager == null) {
            connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(100);
            connManager.setDefaultMaxPerRoute(20);
        }
        
        CloseableHttpClient client =   
            HttpClients.custom()
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .disableRedirectHandling()
                .build();
        clients.add(client);
        return client;
    }
    
    public static synchronized CloseableHttpClient getClientWithBasicAthentication(String username, String password) {
        if(connManager == null) {
            connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(100);
            connManager.setDefaultMaxPerRoute(20);
        }
        
        CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            
        CloseableHttpClient client = 
            HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .build();
        clients.add(client);
        return client;
    }
    
    public static void shutdown() {
        try {
            // Make sure all connections are closed
            for(CloseableHttpClient client: clients) {
                if(client != null) {
                    //Todo: what happens if the connection is already closed?
                    client.close();
                }
            }
        } catch(IOException ex) {
            
        } finally {
            //Always close the manager
            if (connManager != null) {
                connManager.shutdown();
            }
        }
    }
}
