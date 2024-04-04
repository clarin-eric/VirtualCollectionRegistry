/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.core.pid;

import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PidLink;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PidType;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author wilelb
 */
public class PidLinkTest {
    @Test
    public void testIsHandle() {
        assertEquals(true, PidLink.isHandle("hdl:1839/00-0000-0000-0004-2F0A-0"));
        assertEquals(true, PidLink.isHandle("http://hdl.handle.net/1839/00-0000-0000-0004-2F0A-0"));
        assertEquals(true, PidLink.isHandle("https://hdl.handle.net/1839/00-0000-0000-0004-2F0A-0"));
        assertEquals(false, PidLink.isHandle("doi:123/456"));
        assertEquals(true, PidLink.isDoi("doi:123/456"));
        
        assertEquals(false, PidLink.isHandle("1839/00-0000-0000-0004-2F0A-0")); //missing prefix        
        assertEquals(false, PidLink.isHandle("nbn:abc")); //not supported
    }
    
    @Test
    public void testIsDoi() {
        
    }
    
    @Test
    public void testGetPidType() {
        assertEquals(PidType.HANDLE, PidLink.getPidType("hdl:1839/00-0000-0000-0004-2F0A-0"));
    }
    
    @Test
    public void testGetIdentifuer() {
        assertEquals("1839/00-0000-0000-0004-2F0A-0", PidLink.getHandleIdentifier("hdl:1839/00-0000-0000-0004-2F0A-0"));
    }
}
