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

import jakarta.ws.rs.NotFoundException;
import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class DtrClientTest {
    private final static Logger logger = LoggerFactory.getLogger(DtrClientTest.class);
 
    private final DtrClientConfig config = new DtrClientConfig("127.0.0.1", 8888);
        
    private DtrApiMock apiMock;
    
    @Before
    public void setUp() throws IOException {        
        apiMock = new DtrApiMock();
        DtrType type = new DtrType();
        type.name = "Test";
        type.references = new DtrType.DtrReference[]{
            new DtrType.DtrReference("RFC", "http://example.com/rfc"),
            new DtrType.DtrReference("SCHEMA", "http://example.com/schema"),
            new DtrType.DtrReference("CROSSWALK", "http://example.com/crosswalk"),
        };
                
        apiMock.registerType("21.T11969/710b1a3647d431e205e0", type);        
        apiMock.start(config.getPort());
    }

    @After
    public void tearDown() throws IOException {
        if(apiMock != null) {
            apiMock.shutdown();
        }
    }
    
    @Test
    public void testTypeResolution() {
        DtrType type = new DtrClientImpl(config).getType("21.T11969/710b1a3647d431e205e0");
        Assert.assertNotNull(type);
        Assert.assertEquals( 3, type.references.length);
    }
    
    @Test
    public void testCrosswalkFromTypeResolution() {
        String crosswalkUrl = new DtrClientImpl(config).getExtendedTypeCrosswalk("21.T11969/710b1a3647d431e205e0");
        Assert.assertNotNull(crosswalkUrl);
        Assert.assertEquals( "http://example.com/crosswalk", crosswalkUrl);
    }
    
    @Test(expected = NotFoundException.class)
    public void testTypeNotFound() {
        new DtrClientImpl(config).getType("21.T11969/710b1a3647d431e205e0notfound");
    }
}
