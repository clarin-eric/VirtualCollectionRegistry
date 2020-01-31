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
package eu.clarin.cmdi.virtualcollectionregistry;

import org.apache.wicket.request.resource.ContextRelativeResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 *
 * @author wilelb
 */
public class JavaScriptResources {
    private final static ResourceReference BOOTSTRAP = new ContextRelativeResourceReference("script/bootstrap.js"); //bootstrap scripts are extracted from CLARIN's base style bootstrap package
    //private final static ResourceReference BOOTSTRAP_MIN = new ContextRelativeResourceReference("script/bootstrap.min.js"); 

    public static ResourceReference getBootstrapJS() {
        return BOOTSTRAP;
    }
    
    //public static ResourceReference getBootstrapMinJS() {
    //    return BOOTSTRAP_MIN;
    //}
}
