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
package eu.clarin.cmdi.virtualcollectionregistry.model.info;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author wilelb
 */
@XmlRootElement
public class Info {
 
    private String DbVersion;

    /**
     * @return the DbVersion
     */
    public String getDbVersion() {
        return DbVersion;
    }

    /**
     * @param DbVersion the DbVersion to set
     */
    public void setDbVersion(String DbVersion) {
        this.DbVersion = DbVersion;
    }
}
