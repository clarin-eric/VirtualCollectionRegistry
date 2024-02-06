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
package eu.clarin.cmdi.virtualcollectionregistry.model.pid;

/**
 *
 * @author wilelb
 */
public enum PidType {
    DOI("DOI"), HANDLE("HDL"), NBN("NBN"), UNKOWN("UNK");
    
    private final String short_name;

    PidType(String short_name) {
        this.short_name = short_name;
    }

    public String getShort() {
        return this.short_name;
    }

    public static PidType fromString(String short_name) {
        for (PidType b : PidType.values()) {
            if (b.short_name.equalsIgnoreCase(short_name)) {
                return b;
            }
        }
        return null;
    }
}
