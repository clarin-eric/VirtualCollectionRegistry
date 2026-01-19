/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryOptions;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 *
 * @author wilelb
 */
public class QueryFactorySerializer {
    
    private final static ObjectMapper mapper = new ObjectMapper();
    
    public static String serialize(QueryFactory f) throws JsonProcessingException {    
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper.writeValueAsString(f.getQueryOptions());
    }
    
    public static String serializeBase64Encoded(QueryFactory f) throws JsonProcessingException {
        String json = serialize(f);
        String encoded = Base64.getUrlEncoder()
                               .withoutPadding() 
                               .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        return encoded;
    }
    
    public static QueryFactory deserializeBase64Encoded(String encoded) throws JsonProcessingException {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
        String s = new String(decodedBytes, StandardCharsets.UTF_8);
        return deserialze(s);
    }
    
    public static QueryFactory deserialze(String json) throws JsonProcessingException {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        QueryOptions opts = mapper.readValue(json, QueryOptions.class);
        return new QueryFactory(opts);
    }
}
