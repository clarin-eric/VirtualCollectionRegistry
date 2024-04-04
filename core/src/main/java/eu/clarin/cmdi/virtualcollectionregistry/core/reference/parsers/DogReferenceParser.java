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

/**
 *
 * Try to parse any unkown metadata schema via the MSCR crosswalk functionality
 * 
 * @author wilelb
 */
public class DogReferenceParser implements ReferenceParser {
    private final static String PARSER_ID = "PARSER_DOG";
    
    @Override
    public String getId() {
        return PARSER_ID;
    }
    
    @Override
    public boolean parse(String xml, String mimeType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ReferenceParserResult getResult() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
