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
package eu.clarin.cmdi.virtualcollectionregistry.pid;

import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.wicket.components.pid.PidType;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author wilelb
 */
public class HandleLinkModelTest {
    @Test
    public void testIsHandle() {
        assertEquals(true, HandleLinkModel.isHandle("hdl:1839/00-0000-0000-0004-2F0A-0"));
        assertEquals(true, HandleLinkModel.isHandle("http://hdl.handle.net/1839/00-0000-0000-0004-2F0A-0"));
        assertEquals(true, HandleLinkModel.isHandle("http2://hdl.handle.net/1839/00-0000-0000-0004-2F0A-0"));
        
        assertEquals(false, HandleLinkModel.isHandle("1839/00-0000-0000-0004-2F0A-0"));
        assertEquals(false, HandleLinkModel.isHandle("doi:123/456"));
        assertEquals(false, HandleLinkModel.isHandle("nbn:abc"));
    }
    
    @Test
    public void testIsDoi() {
        
    }
    
    @Test
    public void testGetPidType() {
        assertEquals(PidType.HANDLE, HandleLinkModel.getPidType("hdl:1839/00-0000-0000-0004-2F0A-0"));
    }
    
    @Test
    public void testGetIdentifuer() {
        assertEquals("1839/00-0000-0000-0004-2F0A-0", HandleLinkModel.getHandleIdentifier("hdl:1839/00-0000-0000-0004-2F0A-0"));
    }
}
