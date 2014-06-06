/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.virtualcollectionregistry.rest;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * Provider of the singleton injectable {@link VirtualCollectionRegistry}
 * instance
 *
 * @see VirtualCollectionRegistry#instance()
 */
@Provider
public class RegistryInjectableProvider extends SingletonTypeInjectableProvider<Context, VirtualCollectionRegistry> {

    public RegistryInjectableProvider() {
        super(VirtualCollectionRegistry.class, VirtualCollectionRegistry.instance());
    }

}
