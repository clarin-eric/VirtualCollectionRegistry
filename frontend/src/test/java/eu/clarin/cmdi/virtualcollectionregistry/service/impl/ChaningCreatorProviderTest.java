/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.security.Principal;
import java.util.Arrays;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class ChaningCreatorProviderTest {

    private final Mockery context = new JUnit4Mockery();

    /**
     * Test of getCreator method, of class ChaningCreatorProvider.
     */
    @Test
    public void testGetCreator() {
        final CreatorProvider provider1 = context.mock(CreatorProvider.class, "p1");
        final Creator creator1 = new Creator();
        creator1.setAddress("addr1");
        creator1.setEMail("email1");
        creator1.setOrganisation("org1");
        
        final CreatorProvider provider2 = context.mock(CreatorProvider.class, "p2");
        final Creator creator2 = new Creator();
        creator2.setFamilyName("pers");
        creator2.setGivenName("2");
        creator2.setRole("role2");
        creator2.setTelephone("tel2");
        creator2.setWebsite("web2");

        Creator expResult = new Creator();
        expResult.setAddress("addr1");
        expResult.setEMail("email1");
        expResult.setOrganisation("org1");
        expResult.setFamilyName("pers");
        expResult.setGivenName("2");
        expResult.setRole("role2");
        expResult.setTelephone("tel2");
        expResult.setWebsite("web2");
        
        final Principal principal = new Principal() {

            @Override
            public String getName() {
                return "name";
            }
        };
        
        context.checking(new Expectations(){{
            oneOf(provider1).getCreator(principal);
            will(returnValue(creator1));
            oneOf(provider2).getCreator(principal);
            will(returnValue(creator2));
        }});
        final ChaningCreatorProvider instance = new ChaningCreatorProvider(Arrays.asList(provider1, provider2));
        final Creator result = instance.getCreator(principal);
        
        assertEquals(expResult, result);
    }

}
