package eu.clarin.cmdi.virtualcollectionregistry.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.impl.PidWriterImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.LinkedList;
import java.util.List;

@Profile("vcr.pid.multi")
@org.springframework.context.annotation.Configuration
public class MultiPersistentIdentifierConfiguration {

    @Value("${pid_provider.epic.service_base_url}")
    private String epicServiceBaseUrl;
    @Value("${pid_provider.epic.handle_prefix}")
    private String epicHandlePrefix;
    @Value("${pid_provider.epic.user}")
    private String epicUser;
    @Value("${pid_provider.epic.password}")
    private String epicPassword;

    @Value("${pid_provider.doi.service_base_url}")
    private String doiServiceBaseUrl;
    @Value("${pid_provider.doi.handle_prefix}")
    private String doiHandlePrefix;
    @Value("${pid_provider.doi.user}")
    private String doiUser;
    @Value("${pid_provider.doi.password}")
    private String doiPassword;

    @Bean
    public List<PersistentIdentifierProvider> getOtherProviders() {
        //EPIC provider as primary
        PersistentIdentifierProvider primary = new EPICPersistentIdentifierProvider(
                new PidWriterImpl(),
                new Configuration(epicServiceBaseUrl, epicHandlePrefix, epicHandlePrefix, epicPassword));
        primary.setPrimaryProvider(true);

        //DOI provider for citable identifiers
        PersistentIdentifierProvider doi = new DoiPersistentIdentifierProvider(
                new DoiPidWriter(),
                new Configuration(doiServiceBaseUrl, doiHandlePrefix, doiUser, doiPassword));

        List<PersistentIdentifierProvider> list = new LinkedList<>();
        list.add(primary);
        list.add(doi);
        return list;
    }

}
