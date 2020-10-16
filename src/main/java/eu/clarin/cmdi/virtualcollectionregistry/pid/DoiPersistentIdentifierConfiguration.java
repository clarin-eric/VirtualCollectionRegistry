/*
 * Copyright (C) 2020 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.impl.PidWriterImpl;
import de.uni_leipzig.asv.clarin.webservices.pidservices2.interfaces.PidWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author wilelb
 */
@Profile("vcr.pid.doi")
@org.springframework.context.annotation.Configuration
public class DoiPersistentIdentifierConfiguration {
    @Value("${pid_provider.doi.service_base_url}")
    private String serviceBaseUrl;
    @Value("${pid_provider.doi.handle_prefix}")
    private String handlePrefix;
    @Value("${pid_provider.doi.user}")
    private String user;
    @Value("${pid_provider.doi.password}")
    private String password;
    
    @Bean
    public Configuration configuration() {
        return new Configuration(serviceBaseUrl, handlePrefix, user, password);
    }

    @Bean
    public PidWriter pidWriter() {
        return new PidWriterImpl();
    }
}
