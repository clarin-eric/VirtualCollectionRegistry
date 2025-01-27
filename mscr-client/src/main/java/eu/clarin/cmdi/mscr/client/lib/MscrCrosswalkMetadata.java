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
package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

/**
 *
 * @author wilelb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MscrCrosswalkMetadata {
    @JsonProperty(value = "sourceSchema")
    public String sourceSchema;
    @JsonProperty(value = "targetSchema")
    public String targetSchema;

    @JsonProperty(value = "label")
    public HashMap<String, String> label;
    
    
    @JsonProperty(value = "format")
    public String format;
    
    @JsonProperty(value = "fileMetadata")
    public FileMetadata[] files;
    
    public static class FileMetadata {
        @JsonProperty(value = "contentType")
        public String contentType;
        @JsonProperty(value = "size")
        public String size;
        @JsonProperty(value = "fileID")
        public String fileID;
        @JsonProperty(value = "filename")
        public String filename;
        @JsonProperty(value = "timestamp")
        public String timestamp;
    }
}
