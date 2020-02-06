/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author twagoo
 */
public class HandleLinkModelTest {

    private HandleLinkModel instance;
    private IModel<String> model;

    @Before
    public void setUp() {
        model = new Model<>("http://clarin.eu");
        instance = new HandleLinkModel(model);
    }

    @Test
    public void testGetObjectUrl() {
        // non-handles should stay intact
        assertEquals("http://clarin.eu", instance.getObject());
    }

    @Test
    public void testGetObjectHdl() {

        // handle
        model.setObject("hdl:1234/5678-abCD");
        assertEquals("https://hdl.handle.net/1234/5678-abCD", instance.getObject());
    }

    @Test
    public void testGetObjectDOI() {
        // doi
        model.setObject("doi:10.1000/182-Xyz");
        assertEquals("https://dx.doi.org/10.1000/182-Xyz", instance.getObject());
    }

    @Test
    public void testGetObjectURN() {
        // urn:nbn
        model.setObject("urn:nbn:de:bvb:19-146642");
        assertEquals("http://www.nbn-resolving.org/redirect/urn:nbn:de:bvb:19-146642", instance.getObject());
    }

    @Test
    public void testGetObject() {
        // not the model's task to validate content
        model.setObject("Something else");
        assertEquals("Something else", instance.getObject());
    }

}
