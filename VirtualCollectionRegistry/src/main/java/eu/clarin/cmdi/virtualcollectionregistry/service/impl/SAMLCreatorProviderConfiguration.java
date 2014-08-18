package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author twagoo
 */
@Configuration
public class SAMLCreatorProviderConfiguration {

    @Bean
    public CreatorProvider creatorProvider() {
        return new ChaningCreatorProvider(
                Arrays.<CreatorProvider>asList(new SAMLCreatorProvider())
                //TODO: add db provider with fallback to saml
        );
    }

}
