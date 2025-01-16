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
public class PidmrClientTest {
    private final static Logger logger = LoggerFactory.getLogger(PidmrClientTest.class);
 
    private final PidmrClientConfig config = new PidmrClientConfig("127.0.0.1", 8888);
        
    private PidmrApiMock apiMock;
    
    @Before
    public void setUp() throws IOException {        
        apiMock = new PidmrApiMock();
        apiMock
            .registerPid(
                "hdl:1234/5678901", 
                "http://example.com/landingpage1", 
                "http://example.com/metadata1", 
                "http://example.com/resource1")
            .registerPid(
                "hdl:1234/5678902", 
                "http://example.com/landingpage2", 
                "http://example.com/metadata2", 
                "http://example.com/resource2");
        apiMock.start(config.getPort());
    }

    @After
    public void tearDown() throws IOException {
        if(apiMock != null) {
            apiMock.shutdown();
        }
    }
    
    @Test
    public void testLandingPageResolution() throws PidNotFoundException, PidmrException {        
        String url = new PidmrClientImpl(config).resolvePid("hdl:1234/5678901");
        Assert.assertNotNull(url);
    }
    
    @Test(expected = PidNotFoundException.class)
    public void testPidNotFound() throws PidNotFoundException, PidmrException {
        new PidmrClientImpl(config).resolvePid("hdl:1234/doesnotexist");
    }
}
