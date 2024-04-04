/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wilelb
 */
public class ReferenceParserResult {
    public final static String KEY_ERROR = "ERROR";
    public final static String KEY_NAME = "NAME";
    public final static String KEY_DESCRIPTION = "DESCRIPTION";
 
    
    private final Map<String, String> properties = new HashMap<>();
    
    //public ReferenceParserResult() { }

    public void add(String key, String value) {
        this.properties.put(key, value);
    }
    
    public String get(String key) {
        return this.properties.get(key);
    }
}
