/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v1.forms;

import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedByQuery;
import java.io.Serializable;

/**
 *
 * @author wilelb
 */
public class CollectionQuery implements Serializable {
        private String description;
        private String uri;
        private String profile;
        private String value;

        public static CollectionQuery fromGeneratedBy(GeneratedBy genBy) {
            CollectionQuery qry = new CollectionQuery();
            qry.setDescription(genBy.getDescription());
            qry.setProfile(genBy.getQuery().getProfile());
            qry.setUri(genBy.getURI());
            qry.setValue(genBy.getQuery().getValue());
            return qry;
            
        }
        
        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description the description to set
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * @param uri the uri to set
         */
        public void setUri(String label) {
            this.uri = label;
        }

        /**
         * @return the profile
         */
        public String getProfile() {
            return profile;
        }

        /**
         * @param profile the profile to set
         */
        public void setProfile(String profile) {
            this.profile = profile;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(String value) {
            this.value = value;
        }
        
        public GeneratedBy convertToGeneratedBy() {
            GeneratedBy genBy = new GeneratedBy(this.getDescription());
            genBy.setURI(this.getUri());
            genBy.setQuery(new GeneratedByQuery(this.getProfile(), this.getValue()));
            return genBy;
        }
    
}
