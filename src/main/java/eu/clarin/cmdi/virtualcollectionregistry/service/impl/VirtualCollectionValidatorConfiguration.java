package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidatorFactory;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionValidatorConfiguration {

    @Bean
    public ServiceLocatorFactoryBean validatorFactory() {
        final ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
        factoryBean.setServiceLocatorInterface(VirtualCollectionValidatorFactory.class);
        return factoryBean;
    }

}
