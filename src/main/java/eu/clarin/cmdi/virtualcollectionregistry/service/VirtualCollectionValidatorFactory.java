package eu.clarin.cmdi.virtualcollectionregistry.service;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;

/**
 * A factory for {@link VirtualCollectionValidator}. To be used by singletons
 * ({@literal  e.g.} {@link VirtualCollectionRegistry}) to get fresh validator
 * instances. Implement or use {@link ServiceLocatorFactoryBean}.
 *
 * @author twagoo
 */
public interface VirtualCollectionValidatorFactory {

    VirtualCollectionValidator createValidator();
}
