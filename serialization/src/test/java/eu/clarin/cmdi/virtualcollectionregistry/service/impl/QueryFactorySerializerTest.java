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

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryFactory;
import eu.clarin.cmdi.virtualcollectionregistry.query.QueryOptions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author wilelb
 */
public class QueryFactorySerializerTest {

    private String jsonBase64Encoded = "eyJmaWx0ZXIiOnsidHlwZSI6ImFuZCIsIml0ZW1zIjpbeyJ0eXBlIjoiZXhwciIsInByb3BlcnR5IjoiVkNfUFVCTElDX0xFQUYiLCJyZWxhdGlvbiI6MSwidmFsdWUiOnRydWUsIml0ZW1zIjpbXX0seyJ0eXBlIjoiZXhwciIsInByb3BlcnR5IjoiVkNfU1RBVEUiLCJyZWxhdGlvbiI6NjQsInZhbHVlIjpbInB1YmxpYyIsInB1YmxpY19mcm96ZW4iXSwiaXRlbXMiOltdfV19LCJvcmRlckJ5SXRlbXMiOm51bGx9";
    private String json = "{\"filter\":{\"type\":\"and\",\"items\":[{\"type\":\"expr\",\"property\":\"VC_PUBLIC_LEAF\",\"relation\":1,\"value\":true,\"items\":[]},{\"type\":\"expr\",\"property\":\"VC_STATE\",\"relation\":64,\"value\":[\"public\",\"public_frozen\"],\"items\":[]}]},\"orderByItems\":null}";
    
    @Test
    public void testSerializeAsJson() throws JsonProcessingException {
        QueryFactory qryFactory = new QueryFactory();
        qryFactory.and(QueryOptions.Property.VC_PUBLIC_LEAF, QueryOptions.Relation.EQ, Boolean.TRUE);
        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC);
        states.add(VirtualCollection.State.PUBLIC_FROZEN);
        qryFactory.and(QueryOptions.Property.VC_STATE, QueryOptions.Relation.IN, states);
        
        String localJsonBase64 = QueryFactorySerializer.serializeBase64Encoded(qryFactory);
        Assert.assertNotNull(localJsonBase64);
        Assert.assertTrue(localJsonBase64.equals(jsonBase64Encoded));
        
        byte[] decodedBytes = Base64.getUrlDecoder().decode(localJsonBase64);
        String localJson = new String(decodedBytes, StandardCharsets.UTF_8);
        Assert.assertTrue(localJson.equals(json));
    }
    
    @Test
    public void testDeserializeAsJson() throws JsonProcessingException {
        
        
        QueryFactory qryFactory = QueryFactorySerializer.deserializeBase64Encoded(jsonBase64Encoded);
        Assert.assertNotNull(qryFactory);
        /*
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder _cb = mock(CriteriaBuilder.class);
        
        // When em.getCriteriaBuilder() is called, return our mock cb
        when(em.getCriteriaBuilder()).thenReturn(_cb);
        
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<VirtualCollection> cq = cb.createQuery(VirtualCollection.class);
        Root<VirtualCollection> root = cq.from(VirtualCollection.class);
        if(qryFactory.getQueryOptions() != null) {
            final Predicate where = qryFactory.getQueryOptions().getWhere(null, null, root);
            if (where != null) {
                cq.where(where);
            }
            final Order[] order = qryFactory.getQueryOptions().getOrderBy(null, root);
            if (order != null) {
                cq.orderBy(order);
            }
        }
        */
    }
}
