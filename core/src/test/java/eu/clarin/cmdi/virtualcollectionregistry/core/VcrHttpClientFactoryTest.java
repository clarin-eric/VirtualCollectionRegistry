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
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

/**
 *
 * @author wilelb
 */
public class VcrHttpClientFactoryTest {
    /*
    @Test
    public void testMultipleClients() throws IOException {
        URI uri = URI.create("http://www.google.nl");
        
        HttpContext ctx = new BasicHttpContext();
        CloseableHttpClient client = VcrHttpClientFactory.getClient();
        try {
            HttpResponse response = client.execute(new HttpGet(uri), ctx);
            StatusLine status = response.getStatusLine();
        } finally {
           client.close();
        }
        
        //HttpContext ctx = new BasicHttpContext();
        CloseableHttpClient client2 = VcrHttpClientFactory.getClient();
        try {
            HttpResponse response2 = client2.execute(new HttpGet(uri), ctx);
            StatusLine status2 = response2.getStatusLine();
        } finally {
            client.close();
        }
    }
*/
}
