/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.rest.v1.BaseResourceV1;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.*;

/**
 * @author twagoo
 */
@Path("/")
@OpenAPIDefinition(
    info = @Info(
        title = "Virtual Collection Registry REST API",
        version = "1.0.0",
        description = "The Virtual Collection Registry (VCR) REST API is documented following the open API specification. The UI on this page."
                + "allows you to test and play with the various resources."
                + "<br />Description of the API protocol is available <a href=\"../Protocol.txt\">here</a>."
                + "<br /><br />In addition to the REST API there are submission endpoints available to provide a low "
                + "friction integration with external data catalogues. Documentation is available <a href=\"https://github.com/clarin-eric/VirtualCollectionRegistry/blob/master/doc/Integration.md\">here</a>."
                + "<br />Submission endpoint test pages:"
                + "<ul>"
                + "<li><a target=\"_new\" href=\"../test_vc_submission_extensional.html\">Extensional collections</a></li>"
                + "<li><a target=\"_new\" href=\"../test_vc_submission_intensional.html\">Intensional collections</a></li>"
                + "</ul>"
                + "<br />Code repository is hosted on <a href=\"https://github.com/clarin-eric/VirtualCollectionRegistry\">GitHub</a>."
    ),
    servers = {
        @Server(
            description = "Local API endpoint",
            url = "http://localhost:8080/service"
        )
    }
)
public class BaseResource {

    @Context
    private ResourceContext resourceContext;
   
    @Path("/v1")
    public BaseResourceV1 getApiV1() {
        final BaseResourceV1 resource = resourceContext.getResource(BaseResourceV1.class);
        return resource;
    }
    
   
    
    /**
     * TODO: Implement BaseResourceV2 if needed
     *
    @Path("/v2")
    public BaseResourceV2 getApiV2() {
        final BaseResourceV2 resource =
                resourceContext.getResource(BaseResourceV2.class);
        return resource;
    }
     */
}
