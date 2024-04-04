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
package eu.clarin.cmdi.virtualcollectionregistry.core.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import eu.clarin.cmdi.virtualcollectionregistry.core.PermaLinkService;
import eu.clarin.cmdi.virtualcollectionregistry.core.PidProviderServiceImpl;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;

import java.io.Serializable;
import java.net.URI;
import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 *  DataCite DOI REST API docs:
 *  - https://support.datacite.org/docs/api-create-dois
 *  - https://support.datacite.org/reference/dois-2
 *
 *  Schema:
 *  - https://support.datacite.org/docs/schema-mandatory-properties-v43
 *  - https://github.com/datacite/schema/blob/aa5db56897b6ed255e6f2c5d14cfdcbff165567e/source/json/kernel-4.3/datacite_4.3_schema.json
 *
 *  Fabrica
 *  - test: https://doi.test.datacite.org/dois/
 *
 *
 * @author wilelb
 */
@Service
@Profile("vcr.pid.doi")
public class DoiPersistentIdentifierProvider implements PersistentIdentifierProvider, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DoiPersistentIdentifierProvider.class);

    private final transient DoiPidWriter pidWriter;
    private final transient Configuration configuration;
    private final String id = "DOI";
    private boolean primary = false;
    private String infix;

    @Override
    public String getId() {
        return id;
    }

    /**
     *
     * @param pidWriter PID writer implementation to use
     * @param configuration configuration to be passed to PID writer methods
     */
    @Autowired
    public DoiPersistentIdentifierProvider(DoiPidWriter pidWriter, Configuration configuration) {
        this.pidWriter = pidWriter;
        this.configuration = configuration;
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc, PermaLinkService permaLinkService) throws VirtualCollectionRegistryException {
        return createIdentifier(vc, "", permaLinkService);
    }

    @Override
    public PersistentIdentifier createIdentifier(VirtualCollection vc, String suffix, PermaLinkService permaLinkService)
            throws VirtualCollectionRegistryException {
        logger.debug("creating doi for virtual collection \"{}\"", vc.getId());
        try {
            final String requestedPid = String.format("%s%d%s", getInfix(), vc.getId(), suffix);
            DoiRequest req =
                    DoiRequestBuilder.createGenerateDoiRequest(configuration.getHandlePrefix(), requestedPid, vc, permaLinkService);
            final String pid = pidWriter.registerNewPID(configuration, req);
            return new PersistentIdentifier(vc, PersistentIdentifier.Type.DOI, primary, pid);
        } catch (HttpException ex) {
            throw new VirtualCollectionRegistryException("Could not create DOI identifier", ex);
        }
     }

    @Override
    public void updateIdentifier(PersistentIdentifier pid, URI target) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteIdentifier(String pid) throws VirtualCollectionRegistryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean ownsIdentifier(String pid) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isPrimaryProvider() {
        return this.primary;
    }

    @Override
    public void setPrimaryProvider(boolean primary) { this.primary = primary; }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    //Make sure we return the default infix value if an empty infix has been set
    @Override
    public String getInfix() {
        if(this.infix == null || this.infix.isEmpty()) {
            return PidProviderServiceImpl.DEFAULT_INFIX;
        }
        return infix;
    }

    @Override
    public PublicConfiguration getPublicConfiguration() {
        return new PublicConfiguration() {
            @Override
            public String getBaseUrl() {
                if(configuration == null) {
                    logger.warn("Configuration is null");
                    return "";
                }
                return configuration.getServiceBaseURL();
            }
            @Override
            public String getPrefix() {
                if(configuration == null) {
                    logger.warn("Configuration is null");
                    return "";
                }
                return configuration.getHandlePrefix();
            }
            @Override
            public String getUsername() {
                if(configuration == null) {
                    logger.warn("Configuration is null");
                    return "";
                }
                return configuration.getUser();
            }
        };
    }
}
