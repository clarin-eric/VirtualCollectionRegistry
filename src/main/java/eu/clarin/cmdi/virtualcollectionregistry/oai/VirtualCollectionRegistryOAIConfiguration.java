package eu.clarin.cmdi.virtualcollectionregistry.oai;

import eu.clarin.cmdi.oai.provider.OAIException;
import eu.clarin.cmdi.oai.provider.Repository;
import eu.clarin.cmdi.oai.provider.impl.OAIProvider;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that defines beans for the OAI Provider to be used by
 * {@link VirtualCollectionRegistry}
 *
 * @author twagoo
 */
//@Configuration
public class VirtualCollectionRegistryOAIConfiguration {

    //@Autowired
    private VirtualCollectionRegistry vcr;

    @Bean
    public OAIProvider oaiProvider() throws OAIException {
        final OAIProvider instance = OAIProvider.instance();
        instance.setRepository(oaiRepository());
        return instance;
    }

    @Bean
    public Repository oaiRepository() {
        return new VirtualColletionRegistryOAIRepository(vcr);
    }

}
