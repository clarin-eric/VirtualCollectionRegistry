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
package eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.mscr;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * {
        "took": 2,
        "timed_out": false,
        "_shards": {
          "failed": 0,
          "successful": 2,
          "total": 2,
          "skipped": 0
        },
        "hits": {
          "total": {
            "relation": "eq",
            "value": 4
          },
          "hits": [...]
        }
      }
 * @author wilelb
 */
public class MscrSearch {
    @JsonProperty(value = "took")
    public int took;
    @JsonProperty(value = "timed_out")
    public boolean timed_out;
    @JsonProperty(value = "_shards")
    public Shards shards;
    @JsonProperty(value = "hits")
    public Hits hits;
    
    public static class Shards {
        @JsonProperty(value = "failed")
        public int failed;
        @JsonProperty(value = "successful")
        public int successful;
        @JsonProperty(value = "total")
        public int total;
        @JsonProperty(value = "skipped")
        public int skipped;
    }
    
    public static class Hits {
        @JsonProperty(value = "total")
        public HitsTotal total;
        @JsonProperty(value = "hits")
        public Object hits;
    }
    
    public static class HitsTotal {
        @JsonProperty(value = "relation")
        public String relation;
        @JsonProperty(value = "value")
        public int value;
    }
}
